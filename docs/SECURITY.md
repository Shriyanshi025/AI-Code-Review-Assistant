# Security Policy & Execution Safety

## Static Security Rule Enforcement
`JavaLanguageAnalyzer` scans for common Java security concerns:
1. **Hardcoded Secrets**: Plain-text passwords, API keys, or tokens assigned to variables.
2. **System Process Invocations**: `Runtime.getRuntime().exec` or `ProcessBuilder` calls.
3. **Container Interruption**: `System.exit()` invocations.
4. **Unsafe Reflection & Deserialization**.

## Untrusted Code Sandbox Isolation
User code runs inside isolated execution containers with CPU, memory, and network limits. API credentials for AI adapters are kept on the server side and are never sent to the browser client.
