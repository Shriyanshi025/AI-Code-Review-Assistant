# System Architecture

## Overview Architecture

```
                  ┌───────────────────────────────────┐
                  │          React + Vite UI          │
                  │   Monaco Editor (Java V1 Focus)   │
                  └─────────────────┬─────────────────┘
                                    │ REST API (/api)
                                    ▼
                  ┌───────────────────────────────────┐
                  │       Spring Boot Backend         │
                  └─────────────────┬─────────────────┘
                                    │
    ┌───────────────────┬───────────┴───────────┬───────────────────┐
    ▼                   ▼                       ▼                   ▼
ProblemService    SubmissionService       ReviewEngine       ProgressService
    │                   │                       │                   │
    ▼                   ▼                       ▼                   ▼
ProblemEntity    SubmissionEntity       ReviewSessionEntity UserProgressEntity
                                                │
                          ┌─────────────────────┼─────────────────────┐
                          ▼                     ▼                     ▼
                  LanguageAnalyzer       ExecutionEngine       AIReviewAdapter
                (JavaLanguageAnalyzer) (ValidationExecEngine) (DeterministicAdapter)
                          │
                          ▼
                   JavaParser AST
```

## Component Boundaries
1. **`com.codereview.backend.analysis`**: AST parsing (`JavaParser`), cyclomatic complexity calculation, nesting depth, zero-init pitfall checking, security rules.
2. **`com.codereview.backend.execution`**: Isolated test case execution engine.
3. **`com.codereview.backend.ai`**: AI Review adapter producing structured summaries, hints, and refactoring plans.
4. **`com.codereview.backend.review`**: Orchestrates analysis, execution, and AI reasoning into a unified review session.
