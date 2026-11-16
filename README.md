> **A modern, intelligent coding platform with deep AI code understanding, AST code review, quality analysis, complexity evaluation, testing assistance, and progressive hints.**

---

## 🚀 Overview

This platform is a web-based intelligent coding environment designed around **Code and Coding Problems**. Unlike standard compilers or basic project management platforms, this platform combines:

- **Coding Practice & Challenges**: Discover problems, solve in Monaco Editor, execute test suites.
- **Real Java AST Static Code Analysis**: Powered by JavaParser to analyze AST structures, control flows, loops, and zero-initialization pitfalls.
- **Deterministic Engineering Metrics**: Cyclomatic complexity, nesting depth, time complexity ($O(N)$), space complexity ($O(1)$), maintainability index, security, and quality scores.
- **AI Semantic Review Adapter**: High-level reasoning, logical explanation, refactoring recommendations, edge case generation, and progressive hints.
- **Dual Mode Experience**: Practice Mode (coding challenges with test suites) & Free-form Code Review Mode (arbitrary snippet review).

---

## 🛠️ Technology Stack

### Backend
- **Java 21**: Modern Java syntax & records.
- **Spring Boot 3.3.5**: Web, Data JPA, Validation, Actuator.
- **JavaParser Core 3.26.3**: Real structural AST parsing & static rules evaluation.
- **H2 & PostgreSQL**: In-memory test & production database architecture.
- **JUnit 5 & Mockito**: Full unit & integration testing suite.

### Frontend
- **React + Vite**: Fast, modern frontend framework.
- **Monaco Editor**: High-performance code editor with Java syntax highlighting.
- **Vanilla CSS**: Custom dark mode developer design system with glassmorphism and glowing badges.
- **Lucide Icons**: Crisp developer platform iconography.

---

## 🏗️ Quick Start

### 1. Backend Setup & Tests
```bash
cd code-review-backend
mvn clean test
mvn spring-boot:run
```
*Backend runs on `http://localhost:8080` with H2 console available at `/h2-console`.*

### 2. Frontend Setup & Build
```bash
cd frontend
npm install
npm run build
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

---

## 📚 Documentation

Detailed system architecture and design documentation:

- 📖 [`docs/PRODUCT_VISION.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/PRODUCT_VISION.md) - Product Philosophy & Core User Workflows
- 🏗️ [`docs/ARCHITECTURE.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/ARCHITECTURE.md) - System Architecture & Component Interactions
- 🔍 [`docs/ANALYSIS_ENGINE.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/ANALYSIS_ENGINE.md) - Real AST Java Parsing Engine Specifications
- ⚡ [`docs/EXECUTION_ENGINE.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/EXECUTION_ENGINE.md) - Isolated Safe Execution Architecture
- 🤖 [`docs/AI_REVIEW.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/AI_REVIEW.md) - AI Semantic Review & Progressive Hint System
- 🛡️ [`docs/SECURITY.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/SECURITY.md) - Security Policy & Untrusted Code Isolation
- 🗺️ [`docs/ROADMAP.md`](file:///f:/Projects/AI%20Code%20Review%20Assistant/docs/ROADMAP.md) - V1 Release & Future Language Expansion Roadmap
