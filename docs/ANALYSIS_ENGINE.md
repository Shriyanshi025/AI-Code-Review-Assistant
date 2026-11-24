# Multi-Language Static Analysis Engine

## Overview
The platform uses a polymorphic **`LanguageAnalyzer`** architecture to perform language-specific syntax validation, structural analysis, and quality metric computation.

Analysis approaches differ by language:
- **Java**: Employs **JavaParser** to build a full Abstract Syntax Tree (AST) for deterministic AST-level analysis and metric extraction.
- **C++**: Uses **compiler-assisted syntax checking** (via the host C++ toolchain) coupled with **heuristic lexical and structural pattern analysis**. It does **not** embed a full C++ AST parser.

## Architecture

```
                       ┌─────────────────────────┐
                       │    LanguageAnalyzer     │
                       └────────────┬────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌─────────────────────────┐                     ┌─────────────────────────┐
│   JavaLanguageAnalyzer  │                     │   CppLanguageAnalyzer   │
│  (JavaParser AST Tree)  │                     │(Compiler Syntax/Heuristic)│
└─────────────────────────┘                     └─────────────────────────┘
```

---

## 1. Java Static Analysis Engine (`JavaLanguageAnalyzer`)

The Java analysis engine uses **`JavaParser`** to construct a complete Abstract Syntax Tree (AST) of user-submitted Java code.

### Key Capabilities
- **Cyclomatic Complexity & Nesting Depth**: Calculates decision points ($M = E - N + 2P$) across:
  - `IfStmt` / `ConditionalExpr`
  - `ForStmt` / `ForEachStmt` / `WhileStmt` / `DoStmt`
  - `SwitchEntry`
  - `CatchClause`
- **Time Complexity Estimator**: Inferred from nested loop AST depth ($O(1)$, $O(N)$, $O(N^2)$, $O(N^3)$) and recursive method call detection.
- **Space Complexity Estimator**: Inferred from collection / array allocations inside methods ($O(1)$ vs $O(N)$).
- **Structural Anti-Pattern & Security Rules**:
  - `JAVA_ZERO_INIT_MAX_PITFALL`: Detects initialization of tracking variables like `maxSoFar = 0` when input arrays contain all negative numbers.
  - `JAVA_EMPTY_CATCH`: Detects swallowed exceptions in empty catch blocks.
  - `JAVA_HARDCODED_SECRET`: Detects sensitive string literal initializations (passwords, keys).
  - `JAVA_STRING_CONCAT_IN_LOOP`: Detects inefficient `+` String accumulation inside loops.
  - `JAVA_UNSAFE_SYSTEM_EXIT`: Flags `System.exit()` calls that threaten host JVM containers.

---

## 2. C++ Static Analysis Engine (`CppLanguageAnalyzer`)

The C++ analysis engine does **not** construct a full C++ Abstract Syntax Tree (AST). Instead, it combines **compiler-based syntax verification** (when a C++ toolchain is available) with **heuristic lexical and structural pattern analysis**.

### Key Capabilities

- **Compiler-Based Syntax Verification & Fallback**:
  - When a supported C++ compiler (`g++`, `clang++`, or MSVC `cl.exe`) is detected on the host system, `CppLanguageAnalyzer` invokes `CppCompilerService.checkSyntax()` in syntax-only mode (`-fsyntax-only` on GCC/Clang, `/Zs` on MSVC) with a 15-second bounded execution timeout.
  - Catches genuine syntax errors, missing `#include` directives, and type mismatches deterministically.
  - **Fallback When No Compiler is Available**: If no C++ compiler is detected on the host system, the analyzer falls back to a limited structural bracket check (`isBracketsBalanced()`), verifying matching pairs of `{ }`, `( )`, and `[ ]` outside comments and string literals.
    *Important Limitation*: This fallback is solely a superficial delimiter check. It cannot detect invalid tokens, type mismatches, missing semicolons, or undeclared identifiers, and is not a reliable substitute for compiler-grade validation.

- **Heuristic Complexity & Structural Analysis**:
  - Computes cyclomatic complexity heuristically by identifying decision keywords and logical branching operators (`if`, `else if`, `for`, `while`, `case`, `catch`, `&&`, `||`, ternary `?`).
  - Estimates maximum control-structure nesting depth via lexical block tracking.
  - *Note*: These calculations are heuristic estimates rather than formal control-flow graph (CFG) reductions.

- **Pattern-Based Security & Quality Rules**:
  - Heuristic rules scan code tokens and syntax structures to flag common C++ pitfalls:
    - `CPP_INSECURE_GETS`: Identifies deprecated `gets()` function calls (`\bgets\s*\(`) susceptible to buffer overflows.
    - `CPP_UNCHECKED_STRING_COPY`: Flags unbounded C-string functions (`\bstrcpy\s*\(` or `\bstrcat\s*\(`) in favor of `std::string` or bounds-checked alternatives.
    - `CPP_CONTAINER_PASS_BY_VALUE`: Flags function signatures passing `std::vector` by value (`\bvector<[^>]+>\s+[a-zA-Z0-9_]+[^&]*`), recommending pass-by-reference (`const std::vector<T>&`). *(Note: Checks `std::vector` only; does not analyze maps, sets, or other STL containers)*.
    - `CPP_RAW_POINTER_POTENTIAL_LEAK`: Flags lines containing dynamic allocation with `new ` if the submitted source code contains no corresponding `delete ` or `delete[] ` statement. *(Note: Checks text presence of `new` vs `delete`/`delete[]`; does not analyze `malloc`, pointer ownership, or RAII scopes)*.
  - *Note*: These pattern checks are heuristic static alerts and do not provide guaranteed detection of all vulnerabilities, memory leaks, or undefined behavior.

- **Context-Aware Testing Suggestions**:
  - Heuristically detects input types by keyword scanning and suggests targeted unit test cases:
    - Array / Vector types (`vector`, `[]`, `arr`): suggests empty container (`[]`), single-element container, and duplicate values.
    - String types (`string`, `char*`, `char `): suggests empty string (`""`), single-character string, and strings containing spaces or special characters.
    - Numeric types (`int `, `long `, `double `): suggests zero (`0`), negative integer boundary values, and constraint limits (`INT_MAX`, `INT_MIN`).
