# Platform Roadmap

## Phase 1 (Initial Foundation)
- [x] Spring Boot 3.3.5 & Java 21 Backend Architecture.
- [x] React + Vite + Monaco Editor Frontend Shell.
- [x] H2 Database (File-based, PostgreSQL compatibility mode) Domain Model (`User`, `Problem`, `TestCase`, `Submission`, `ReviewSession`, `Finding`, `ReviewMetric`, `TestSuggestion`, `Hint`, `UserProgress`).
- [x] Extensible `LanguageAnalyzer` with real `JavaLanguageAnalyzer` (JavaParser AST parsing).
- [x] Unified `ReviewEngine` orchestrating correctness, static metrics, AI reasoning, and scoring.
- [x] Practice Mode & Free-form Code Review Mode.
- [x] Complete test suite passing with 0 failures (`mvn clean test` & `npm run build`).

## Phase 2 (C++ Multi-Language Expansion)
- [x] C++ Programming Language Support (C++17) across Frontend and Backend.
- [x] Dynamic Monaco Editor language switcher (Java / C++17) with draft retention and aligned C++17 labels.
- [x] `CppCompilerService` supporting GCC (`g++`), Clang (`clang++`), and MSVC (`cl.exe`) with C++17 standards.
- [x] `CppExecutionEngine` with ephemeral temporary workspaces, bounded process lifecycle, timeout watchdog, async stream handling, and environment sanitization.
- [x] `CppLanguageAnalyzer` implementing compiler syntax validation (with tri-state VALID/INVALID/UNAVAILABLE verification), complexity heuristics, C++ security heuristics, and testing suggestions.
- [x] Language-aware AI code review prompt integration.
- [x] Dedicated regression test suite with 17 automated tests in `CppExecutionAndCompilationTest` verifying compiler detection, compilation, standard I/O execution, process lifecycle, timeouts, output truncation, token-boundary comparison, and hidden test masking.

## Phase 3 (Production Readiness & Enhancements)
- [ ] User Authentication & Multi-Tenancy (user accounts, session isolation, role-based access).
- [ ] Containerized OS-Level Worker Sandbox (Docker / gVisor / nsjail with network isolation, read-only rootfs, non-root user, cgroups v2 quotas, seccomp filters) — **mandatory prerequisite for public deployment**.
- [ ] Python Programming Language Support (`PythonLanguageAnalyzer` and Python execution engine).
- [ ] Multi-file solution support.
- [ ] Git repository integration for PR reviews.
