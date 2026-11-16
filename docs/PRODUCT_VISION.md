# Product Vision & Philosophy

## Product Name
**AI-Powered Code Review & Intelligent Coding Evaluation Platform**

## Core Philosophy
The platform is designed to answer four fundamental developer questions:
1. **Does my code work?** (Test execution correctness)
2. **Why does it work or fail?** (Detailed AST error explanation & edge-case diagnosis)
3. **How good is my implementation?** (Multi-metric engineering scores: Quality, Complexity, Maintainability, Security, Performance)
4. **How can I improve it?** (Refactoring plans, progressive hints, edge-case test suggestions)

## Core User Experience Workflows

### 1. Practice Mode
```
DISCOVER PROBLEM ──> OPEN PROBLEM ──> READ STATEMENT & HINTS ──> WRITE JAVA CODE 
      │
      ▼
RUN / ANALYZE ──> EXECUTE TEST SUITE ──> REAL AST CODE REVIEW ──> AI REASONING ──> PROGRESSIVE HINTS ──> IMPROVE CODE
```

### 2. Free-Form Code Review Mode
```
OPEN FREE-FORM REVIEW ──> PASTE JAVA CODE ──> RUN AST ANALYZER ──> VIEW LINE FINDINGS ──> COMPLEXITY ESTIMATES ──> REFACTOR
```

## V1 Scope
- **Language**: Java 21 (Single language V1 focus with extensible `LanguageAnalyzer` abstraction).
- **Core Domain**: Code, Coding Problems, Submissions, Review Sessions, Findings, User Progress.
