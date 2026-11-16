# Real Java AST Static Analysis Engine

## Overview
The analysis engine avoids superficial regex pattern matching. It uses **`JavaParser`** to construct a complete Abstract Syntax Tree (AST) of the submitted Java code.

## Key Capabilities

### 1. Cyclomatic Complexity & Nesting Depth
Calculates decision points ($M = E - N + 2P$) across:
- `IfStmt` / `ConditionalExpr`
- `ForStmt` / `ForEachStmt` / `WhileStmt` / `DoStmt`
- `SwitchEntry`
- `CatchClause`

### 2. Time & Space Complexity Estimators
- **Time Complexity**: Inferred from nested loop AST depth ($O(1)$, $O(N)$, $O(N^2)$, $O(N^3)$) and recursive method call detection.
- **Space Complexity**: Inferred from collection / array allocations inside methods ($O(1)$ vs $O(N)$).

### 3. Structural Anti-Pattern & Bug Rules
- **`JAVA_ZERO_INIT_MAX_PITFALL`**: Detects initialization of tracking variables like `maxSoFar = 0` when input arrays contain all negative numbers.
- **`JAVA_EMPTY_CATCH`**: Detects swallowed exceptions in empty catch blocks.
- **`JAVA_HARDCODED_SECRET`**: Detects sensitive string literal initializations (passwords, keys).
- **`JAVA_STRING_CONCAT_IN_LOOP`**: Detects inefficient `+` String accumulation inside loops.
- **`JAVA_UNSAFE_SYSTEM_EXIT`**: Flags `System.exit()` calls that threaten host JVM containers.
