# Platform Roadmap

## Phase 1 (Initial Foundation - COMPLETED)
- [x] Spring Boot 3.3.5 & Java 21 Backend Architecture.
- [x] React + Vite + Monaco Editor Frontend Shell.
- [x] PostgreSQL & H2 Database Domain Model (`User`, `Problem`, `TestCase`, `Submission`, `ReviewSession`, `Finding`, `ReviewMetric`, `TestSuggestion`, `Hint`, `UserProgress`).
- [x] Extensible `LanguageAnalyzer` with real `JavaLanguageAnalyzer` (JavaParser AST parsing).
- [x] Unified `ReviewEngine` orchestrating correctness, static metrics, AI reasoning, and scoring.
- [x] Practice Mode & Free-form Code Review Mode.
- [x] Complete test suite passing with 0 failures (`mvn clean test` & `npm run build`).

## Phase 2 (Upcoming Enhancements)
- [ ] Live LLM Adapter Integration (Gemini / OpenAI API).
- [ ] Containerized Docker Worker Sandbox for execution engine.
- [ ] Multi-file Java solution support.
- [ ] Additional language analyzers (`PythonLanguageAnalyzer`, `CppLanguageAnalyzer`).
