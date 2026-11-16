# AI Semantic Review & Progressive Hint System

## Overview
The AI review layer enhances deterministic AST analysis rather than replacing it.

## Key Principles
1. **Deterministic Ground Truth First**: AI receives structured AST findings, line numbers, and metric scores as input context.
2. **Schema-Enforced Outputs**: AI responses are strictly parsed into `StructuredAIResponse` DTOs containing:
   - Summary
   - Logical reasoning
   - Refactoring plan
   - Alternative approach
   - Edge case test suggestions
   - Progressive hints
3. **No Groundless Inventions**: AI is prohibited from inventing fake compiler output or non-existent line numbers.

## Progressive Hint System
Hints are categorized into progressive tiers:
- **Hint 1**: Conceptual nudge (e.g. "Think about what happens with negative input values").
- **Hint 2**: Strategy direction (e.g. "Consider initializing candidate variables with the first element").
- **Hint 3**: Algorithmic suggestion (e.g. "Use a HashMap or Two-Pointer approach to optimize time complexity").
