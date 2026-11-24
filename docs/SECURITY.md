# Security Policy & Execution Safety

> [!CAUTION]
> **DEPLOYMENT STATUS: TRUSTED LOCAL DEVELOPMENT ONLY — PUBLIC DEPLOYMENT IS BLOCKED**
> The current execution architecture does **not** provide operating system-level container or sandbox isolation. Arbitrary code execution is strictly restricted to trusted local developer environments. Public multi-tenant or untrusted deployment is **STRICTLY BLOCKED** until an isolated container or micro-VM worker architecture is implemented.

---

## 1. Executive Summary & Security Posture

The AI-Powered Code Review & Quality Analysis Platform accepts and evaluates arbitrary source code written by users in **Java** and **C++**.

Executing untrusted code securely requires strong kernel-level isolation (such as Linux namespaces, cgroups, seccomp filters, non-root user execution, and network isolation). In the current implementation:
- **Java execution** runs **in-process** inside the Spring Boot host JVM via dynamic compilation and reflection.
- **C++ execution** runs **out-of-process** via `ProcessBuilder`, executing compiled native binaries on the host operating system with the permissions of the host process user.

While basic process lifecycle controls (timeouts, ephemeral directories, stream truncation, and environment variable scrubbing) are active, **they do not constitute a secure security boundary or sandbox**.

---

## 2. Execution Architecture & Verified Controls

### Java Execution Engine (`ValidationExecutionEngine`)
- **Execution Mechanism**: In-process execution within the Spring Boot JVM. Source code is compiled using `javax.tools.ToolProvider.getSystemJavaCompiler()`, loaded dynamically into a temporary `URLClassLoader`, and executed via reflective method invocation (`Method.invoke()`) inside a single-thread `ExecutorService`.
- **Verified Lifecycle Controls**:
  - **Execution Watchdog**: Wall-clock timeout (default 2000 ms) enforced via `Future.get(timeLimitMs, TimeUnit.MILLISECONDS)`.
  - **Ephemeral Filesystem Workspace**: Class files are compiled into a temporary directory (`java_freeform_exec_*` or `temp_exec_*`) and recursively deleted in `finally` blocks upon completion.
  - **Output Capture & Truncation**: Standard output and error streams are captured and capped at 10,000 characters to prevent memory exhaustion from verbose output.
- **Critical Unmitigated Security Risks**:
  1. **In-Process Execution**: User code runs inside the **same JVM heap and memory space** as the Spring Boot server.
  2. **Host Process Termination**: Malicious calls such as `System.exit()` can terminate the entire application server. (While `JavaLanguageAnalyzer` includes heuristic regex detection, static analysis can be trivially bypassed via dynamic reflection or obfuscated classloading).
  3. **Resource Exhaustion (DoS)**: Unbounded heap allocations (e.g., massive arrays) trigger JVM-wide `OutOfMemoryError`. Unbounded thread creation or non-interruptible infinite loops can exhaust server resources.
  4. **Global Stream Mutation**: Freeform execution temporarily replaces global `System.out` and `System.err`, impacting server logging and interfering with concurrent threads.
  5. **Direct Host Access**: User code has full access to host files, sockets, and memory accessible to the backend process user.

### C++ Execution Engine (`CppExecutionEngine` & `CppCompilerService`)
- **Execution Mechanism**: Out-of-process subprocess execution. Source code is compiled into an executable binary using the host toolchain (MSVC `cl.exe`, GCC `g++`, or Clang `clang++`) and executed as a child process via Java `ProcessBuilder`.
- **Verified Lifecycle Controls**:
  - **Environment Variable Scrubbing**: Subprocess environment is explicitly cleared (`pb.environment().clear()`), preventing leakage of backend secrets and API keys (such as `GEMINI_API_KEY`).
  - **Ephemeral Workspaces**: Dedicated temporary directories (`temp_cpp_exec_*`) isolate compilation artifacts and executables; temporary files (`.obj`, `.exe`) are cleaned up upon completion or timeout.
  - **Execution & Compilation Watchdogs**: Strict timeouts (2000 ms execution, 30 s compilation, 15 s syntax check) enforced via watchdog threads; timeouts trigger `process.destroyForcibly()`.
  - **Asynchronous Stream Draining & Deadlock Prevention**: Reader threads consume `stdout` and `stderr` concurrently to prevent OS pipe buffer deadlocks.
  - **Bounded Output Retention**: `stdout` and `stderr` are independently capped at 10,000 characters (`MAX_OUTPUT_CHARS = 10000`) per stream, with truncation markers appended while continuing to drain excess bytes to EOF.
  - **Non-blocking Stdin**: Input writing is performed asynchronously with timeout protection so unread stdin cannot hang the runner.
- **Critical Unmitigated Security Risks**:
  1. **Host OS User Privileges**: The compiled binary runs with the full permissions of the host operating system user running the backend process.
  2. **No Filesystem Virtualization**: There is no chroot, jail, or filesystem container. The binary can read and modify any file accessible to the host user.
  3. **No Network Isolation**: There is no network namespace restriction. The binary can open sockets, connect to the local network, or access external endpoints.
  4. **No Kernel Hardware Quotas**: Operating system-level memory caps and CPU quotas (e.g. cgroups) are not enforced; code can consume host RAM and CPU cores until the watchdog timer fires.
  5. **Process Tree Escapes**: `destroyForcibly()` terminates the immediate child process, but child processes spawned by an untrusted binary could be orphaned on certain operating systems.

---

## 3. Static Security Heuristics (Defense-in-Depth)

The static analysis layer performs heuristic pattern checks prior to execution:
- **`JavaLanguageAnalyzer`**:
  - Flags plain-text secrets and hardcoded API tokens.
  - Flags process invocation patterns (`Runtime.getRuntime().exec`, `ProcessBuilder`).
  - Flags JVM termination calls (`System.exit()`).
  - Flags unsafe reflection and serialization patterns.
- **`CppLanguageAnalyzer`**:
  - Flags legacy unsafe C-string functions (`strcpy`, `gets`, `sprintf`).
  - Flags unmanaged dynamic allocations without corresponding `delete`/`free`.
  - Flags pass-by-value STL containers (`std::vector`).

> [!WARNING]
> Static analysis rules provide educational quality feedback and defense-in-depth, but **cannot be relied upon as a security boundary**. Obfuscated code, dynamic class loading, and indirect invocation can bypass static checks.

---

## 4. Credential & Data Security

- **Server-Side API Key Storage**: Sensitive credentials, including the Google Gemini API key (`GEMINI_API_KEY`), are stored in backend configuration/environment variables and are **never** transmitted to the frontend or exposed in API responses.
- **Subprocess Environment Isolation**: Subprocess execution explicitly purges all environment variables (`pb.environment().clear()`) so native binaries cannot read server environment variables.
- **Test Case Masking**: Hidden test cases mask inputs, expected outputs, and actual outputs in execution responses to prevent leaking solution verification criteria.

---

## 5. Deployment Readiness Verdict

| Environment | Supported? | Justification |
| :--- | :--- | :--- |
| **Local Developer Machine** | **YES** | Trusted single-developer environment. Process lifecycle controls manage runaway loops and timeouts. |
| **Isolated CI/CD Pipeline** | **YES** | Provided the CI/CD container/VM itself is ephemeral, non-privileged, and discarded after test execution. |
| **Shared Staging / Multi-User** | **BLOCKED** | Lack of user authentication and lack of process isolation allow cross-tenant interference. |
| **Public / Untrusted Internet** | **BLOCKED** | Untrusted code execution without OS-level sandboxing constitutes a critical Remote Code Execution (RCE) and Denial of Service (DoS) vulnerability. |

---

## 6. Recommended Phase 3 Production Sandbox Architecture

To safely transition the platform to public, multi-tenant deployment, arbitrary code execution must be decoupled into an isolated worker service implementing true OS-level isolation:

```
[ Spring Boot API Gateway ]
          │  (mTLS / Authenticated Message Queue)
          ▼
[ Ephemeral Execution Worker Pool ]
          │
          ├── [ Docker / Podman / containerd Container ]
          │     ├── Read-only root filesystem (--read-only)
          │     ├── Size-bounded tmpfs for workspace (--tmpfs /workspace:rw,size=64M,noexec,nodev)
          │     ├── Complete network disablement (--network none)
          │     ├── Dropped Linux capabilities (--cap-drop=ALL)
          │     ├── Non-root unprivileged execution (UID 10001:10001)
          │     ├── cgroups v2 resource quotas (memory: 256MB, CPU: 1.0 core)
          │     ├── PID limits (--pids-limit 32)
          │     └── Custom Seccomp profile blocking unsafe syscalls (ptrace, socket, mount, reboot)
          │
          └── Alternative: Micro-VM / Sandbox Runtime (gVisor runsc / nsjail / Firecracker)
```

### Minimum Required Production Controls:
1. **Isolated Execution Host**: Execution workers must run on dedicated nodes physically or virtually isolated from the database and API services.
2. **Network Isolation**: Complete network namespace disablement (`--network none`).
3. **Filesystem Confinement**: Read-only root image with a non-executable, sized-bounded temporary filesystem (`tmpfs`) for compilation and output.
4. **Kernel Quotas (cgroups v2)**: Strict memory quotas (e.g. 256 MB) that immediately terminate out-of-memory executions; CPU quotas (e.g. 1 core) to prevent host exhaustion.
5. **Privilege Demotion**: Mandatory non-root user execution with all Linux capabilities dropped (`--cap-drop=ALL`).
6. **Syscall Filtering**: Seccomp filters or user-space kernel virtualization (gVisor `runsc`) to intercept and deny system calls that touch kernel structures or host hardware.
7. **Process Tree Termination**: Cgroup-based process destruction ensuring all child and grandchild processes are cleanly purged on timeout or exit.