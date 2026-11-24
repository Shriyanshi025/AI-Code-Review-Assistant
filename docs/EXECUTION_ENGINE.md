# Execution Engine Architecture

## Overview
User-submitted code is untrusted. The execution engine evaluates submissions across supported programming languages (Java and C++) through two distinct execution pipelines:
- **Java**: In-process execution within the host JVM using dynamic compilation (`ToolProvider`), dynamic class loading (`URLClassLoader`), and reflective method execution inside a single-thread executor.
- **C++**: Out-of-process execution as an external operating system subprocess managed via `ProcessBuilder`, executing native binaries compiled by the host C++ toolchain (MSVC, GCC, or Clang).

## Architecture

```
[ User Solution ] ──> [ Language Normalization ] ──> [ Execution Engine ] ──> [ Structured Execution Result ]
                                                             │
                             ┌───────────────────────────────┴───────────────────────────────┐
                             ▼                                                               ▼
                 [ Java Execution Pipeline ]                                     [ C++ Execution Pipeline ]
                 - In-process JVM execution via Reflection                       - Out-of-process via ProcessBuilder
                 - ToolProvider dynamic compiler                                 - Auto-detects g++, clang++, MSVC
                 - URLClassLoader in ephemeral temp dir                          - Ephemeral temporary directory
                 - Single-thread executor with Future.get()                      - Subprocess pipes (stdin/stdout/stderr)
                 - Wall-clock timeout: 2000 ms per test case                     - Watchdog timer: 2000 ms timeout
                 - Output capture & truncation (10,000 chars)                    - Output cap: 10,000 chars per stream
                 - Shared JVM heap and host permissions                          - Environment variable scrubbing
```

---

## 1. Process Management & Execution Lifecycle Controls

### Java Execution Lifecycle (`ValidationExecutionEngine`)
1. **Dynamic Compilation**:
   - Compiles user source code using `javax.tools.ToolProvider.getSystemJavaCompiler()`.
   - If class name is missing, wraps snippet in a default class (`Solution`).
   - If compiler is unavailable, returns a structured `NOT_APPLICABLE` or fallback result.
2. **Dynamic Class Loading**:
   - Compiles `.class` files into an ephemeral temporary directory (`java_freeform_exec_*` or `temp_exec_*`).
   - Loads the compiled class via an isolated `URLClassLoader`.
3. **Reflective Invocation & Timeouts**:
   - Identifies the entry point (`main(String[])`, `main()`, or target problem method).
   - Executes the method reflectively inside a dedicated single-thread `ExecutorService`.
   - Enforces a 2000 ms timeout via `Future.get(timeLimitMs, TimeUnit.MILLISECONDS)`.
4. **Stream Redirection & Truncation**:
   - In freeform execution, captures output via custom `PrintStream` instances.
   - Caps retained output at 10,000 characters to prevent memory exhaustion.
5. **Ephemeral Directory Cleanup**:
   - Class files and temporary source directories are recursively deleted in `finally` blocks.

### C++ Subprocess Lifecycle (`CppExecutionEngine`)
1. **Compilation via Subprocess (`CppCompilerService`)**:
   - Detects available toolchain (MSVC `cl.exe`, GCC `g++`, Clang `clang++`).
   - Executes compiler as a separate subprocess with a 30-second watchdog and 20,000 character output cap.
   - Compiles with C++17 standard (`-std=c++17` or `/std:c++17`) and optimization (`-O2` or `/O2`).
2. **Ephemeral Workspaces**:
   - Each submission compiles and executes in a dedicated temporary directory (`temp_cpp_exec_*`).
   - Source files, compiled executables, and compiler intermediate files (`.obj`) are cleaned up upon completion or timeout via `finally` blocks.
3. **Environment Sanitization**:
   - Subprocess environment is explicitly sanitized before launching (`pb.environment().clear()`).
   - Sensitive host environment variables and backend API keys (such as `GEMINI_API_KEY`) are removed to prevent leakage to user code.
4. **Bounded Process Lifecycle & Timeouts**:
   - Strict 2000 ms wall-clock watchdog per test case.
   - On timeout, `process.destroyForcibly()` is issued, and streams are closed.
5. **Output Capture, Draining, and Truncation (`MAX_OUTPUT_CHARS = 10000` per stream)**:
   - Asynchronous reader threads consume `stdout` and `stderr` independently immediately upon process startup to prevent pipe buffer deadlocks.
   - Stdin writing occurs asynchronously so that blocked writes cannot hang or bypass the execution timeout.
   - Output retention is bounded **per stream**: `stdout` and `stderr` are each capped independently at 10,000 characters (`MAX_OUTPUT_CHARS = 10000`).
   - When output on either stream exceeds 10,000 characters, readers continue draining the remaining bytes until EOF so the child process does not block on filled pipe buffers, and `\n[Output truncated...]` is appended to that stream's retained output.
6. **Information Leakage Prevention**:
   - Hidden test case inputs, expected outputs, actual outputs, and detailed failure messages are masked to prevent exposing test suite internals.
7. **Structured Execution Results**:
   - Output responses conform strictly to: `PASSED`, `FAILED`, `COMPILATION_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `NOT_APPLICABLE`.

---

## 2. Security Boundaries and Sandbox Limitations

> [!WARNING]
> **Neither Java in-process execution nor C++ ProcessBuilder execution is an operating system sandbox.**
> Ephemeral directories, timeouts, stream truncation, and environment sanitization provide basic process lifecycle controls, but they do **not** form a secure sandbox boundary.

- **Java In-Process Vulnerabilities**: Java execution shares the Spring Boot JVM memory and process space. Malicious code could attempt `System.exit()`, trigger JVM `OutOfMemoryError`, spawn unmanaged threads, or inspect host memory.
- **Host User Privileges**: User-submitted C++ code executes with the exact same operating system user permissions as the backend Spring Boot process.
- **No Filesystem Isolation**: While ephemeral directories are used for compilation and execution, there is no chroot, jail, or filesystem virtualization. The process can access any host filesystem paths readable by the host process user.
- **No Network Isolation**: There is no network namespace isolation. The subprocess can initiate network sockets and establish outbound or local network connections if permitted by the host OS.
- **No OS Memory/Hardware Quotas**: The system does not enforce operating system-level memory caps (e.g. no cgroups, memory limits, or CPU quotas).
- **Public & Untrusted Deployment**: Container-based or OS-level sandboxing (e.g. Docker containers with non-root execution, dropped Linux capabilities, read-only root filesystems, `--network none`, seccomp filters, and cgroups, or Windows Job Objects / AppContainer) is **planned future work (Phase 3)** and is **strictly required** before deploying this platform to public or untrusted multi-tenant environments. Public deployment is **strictly blocked** until Phase 3 sandboxing is implemented.

---

## 3. C++ Compiler Adapter (`CppCompilerService`)

The backend automatically detects and leverages the available C++ toolchain:
- **GCC (`g++`)**: Standard Linux/macOS/Windows MinGW environments.
- **Clang (`clang++`)**: LLVM toolchain.
- **MSVC (`cl.exe`)**: Microsoft Visual Studio / Build Tools (auto-discovers `vswhere.exe` and `vcvarsall.bat` on Windows).
- **Custom Compiler**: Configurable via `cpp.compiler.path` and `cpp.vcvarsall.path` in `application.properties`.
- **Standards & Flags**: Uses C++17 (`-std=c++17` on GCC/Clang, `/std:c++17` on MSVC) with optimization enabled (`-O2` / `/O2`).
- **Compilation Timeout**: 30-second watchdog with asynchronous stream draining and output capped at 20,000 characters (`MAX_OUTPUT_CHARS = 20000`).
- **Syntax Check Mode**: 15-second watchdog running syntax-only validation (`-fsyntax-only` or `/Zs`).
- **Artifact Cleanup**: Automatically removes `.obj` files and temporary compilation artifacts on success, error, and timeout.