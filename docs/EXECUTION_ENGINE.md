# Execution Engine Architecture

## Overview
User-submitted code is untrusted. The execution engine architecture strictly decouples arbitrary code execution from the primary Spring Boot host process.

## Architecture & Isolation Contract

```
[ User Solution ] ──> [ AST Pre-Validation ] ──> [ Isolated Sandbox Runner Interface ] ──> [ Structured Execution Result ]
```

### Safety Guarantees
1. **No Host Execution**: Untrusted code is never compiled or executed directly inside the Spring Boot JVM process.
2. **Resource Constraints**:
   - Time Limit: 2000 ms per test case.
   - Memory Limit: 256 MB.
   - Network Access: Completely disabled.
   - Restricted Filesystem: Read-only memory.
3. **Structured Results**:
   - `PASSED` / `FAILED` / `COMPILATION_ERROR` / `RUNTIME_ERROR` / `TIMEOUT`.
   - Hidden test case inputs and outputs are never exposed to the client.
