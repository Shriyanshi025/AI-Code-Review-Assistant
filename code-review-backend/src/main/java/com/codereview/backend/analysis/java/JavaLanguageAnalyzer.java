package com.codereview.backend.analysis.java;

import com.codereview.backend.analysis.core.*;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JavaLanguageAnalyzer implements LanguageAnalyzer {

    @Override
    public String getSupportedLanguage() {
        return "JAVA";
    }

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            return AnalysisResult.builder()
                    .language("JAVA")
                    .syntaxStatus(SyntaxStatus.INVALID)
                    .syntaxValid(false)
                    .parsingErrorMessage("Source code is empty")
                    .qualityScore(0)
                    .complexityScore(0)
                    .maintainabilityScore(0)
                    .securityScore(0)
                    .performanceScore(0)
                    .build();
        }

        CompilationUnit cu;
        try {
            // Attempt standard compilation unit parse; if snippet is missing class wrap, attempt method wrap fallback
            cu = parseJavaCode(code);
        } catch (ParseProblemException e) {
            String errorMsg = e.getProblems().isEmpty() ? e.getMessage() : e.getProblems().get(0).getMessage();
            int errorLine = 1;
            if (!e.getProblems().isEmpty() && e.getProblems().get(0).getLocation().isPresent()) {
                errorLine = e.getProblems().get(0).getLocation().get().getBegin().getRange().map(r -> r.begin.line).orElse(1);
            }

            List<FindingDto> syntaxFindings = new ArrayList<>();
            syntaxFindings.add(FindingDto.builder()
                    .ruleId("JAVA_SYNTAX_ERROR")
                    .severity(FindingSeverity.HIGH)
                    .category(FindingCategory.CORRECTNESS)
                    .title("Java Compilation / Syntax Error")
                    .message("The Java code contains syntax errors and failed AST parsing.")
                    .explanation(errorMsg)
                    .recommendation("Ensure all syntax, braces, semicolons, and class definitions are properly formatted.")
                    .lineStart(errorLine)
                    .lineEnd(errorLine)
                    .confidence(1.0)
                    .source(FindingSource.DETERMINISTIC)
                    .build());

            return AnalysisResult.builder()
                    .language("JAVA")
                    .syntaxStatus(SyntaxStatus.INVALID)
                    .syntaxValid(false)
                    .parsingErrorMessage(errorMsg)
                    .findings(syntaxFindings)
                    .qualityScore(20)
                    .complexityScore(50)
                    .maintainabilityScore(30)
                    .securityScore(90)
                    .performanceScore(50)
                    .build();
        }

        // Real AST Analysis
        List<FindingDto> findings = new ArrayList<>();

        // 1. Cyclomatic Complexity & Nesting Depth
        ComplexityVisitor complexityVisitor = new ComplexityVisitor();
        cu.accept(complexityVisitor, null);
        int totalComplexity = Math.max(1, complexityVisitor.getCyclomaticComplexity());
        int maxNestingDepth = complexityVisitor.getMaxNestingDepth();
        int maxLoopDepth = complexityVisitor.getMaxLoopDepth();
        boolean hasRecursion = complexityVisitor.isHasRecursion();

        // Add Metric Findings with Directional Mentoring Suggestions
        if (maxLoopDepth >= 2) {
            findings.add(FindingDto.builder()
                    .ruleId("JAVA_NESTED_LOOP_TRAVERSAL")
                    .severity(FindingSeverity.MEDIUM)
                    .category(FindingCategory.PERFORMANCE)
                    .title("Repeated / Nested Traversal Detected")
                    .message("Nested loop iteration increases time complexity.")
                    .explanation("Repeated loop traversal over elements may significantly increase runtime execution cost.")
                    .recommendation("This operation repeatedly scans data that may already have been processed. Consider whether you can avoid revisiting the same elements and reduce the amount of repeated work.")
                    .hint("Think about whether previously inspected elements can be remembered or indexed.")
                    .lineStart(1)
                    .lineEnd(1)
                    .confidence(0.9)
                    .source(FindingSource.DETERMINISTIC)
                    .build());
        }

        if (maxNestingDepth >= 3) {
            findings.add(FindingDto.builder()
                    .ruleId("JAVA_DEEP_NESTING")
                    .severity(FindingSeverity.MEDIUM)
                    .category(FindingCategory.COMPLEXITY)
                    .title("Deep Control Flow Nesting")
                    .message("High nesting depth reduces code readability.")
                    .explanation("Multiple nested decision levels obscure control flow and make edge cases difficult to verify.")
                    .recommendation("This section has several nested decision levels, which can make the control flow harder to follow. Consider whether some of the conditions can be simplified or separated into smaller logical steps.")
                    .hint("Consider early returns or guard clauses to reduce nesting depth.")
                    .lineStart(1)
                    .lineEnd(1)
                    .confidence(0.9)
                    .source(FindingSource.DETERMINISTIC)
                    .build());
        }

        // 2. Deterministic Code Quality & Anti-pattern Visitor
        AstRuleVisitor ruleVisitor = new AstRuleVisitor(findings, code);
        cu.accept(ruleVisitor, null);

        // 3. Estimate Time and Space Complexity & Code-Aware Testing Analysis
        String timeComplexity = estimateTimeComplexity(maxLoopDepth, hasRecursion);
        boolean hasInputSizeAlloc = detectInputSizeAllocation(cu);
        String spaceComplexity = hasInputSizeAlloc ? "O(N)" : "O(1)";

        CodeAwareTestingAnalysis testingAnalysis = analyzeInputStructures(cu);

        // 4. Calculate Sub-Scores (0-100 scale based on AST metrics and findings)
        int qualityScore = Math.max(0, 100 - countFindings(findings, FindingCategory.CODE_QUALITY) * 15 - countFindings(findings, FindingCategory.CORRECTNESS) * 20);
        int complexityScore = Math.max(0, 100 - Math.max(0, totalComplexity - 5) * 6 - Math.max(0, maxNestingDepth - 2) * 10 - (hasInputSizeAlloc ? 15 : 0));
        int maintainabilityScore = Math.max(0, 100 - countFindings(findings, FindingCategory.MAINTAINABILITY) * 12 - (totalComplexity > 10 ? 20 : 0));
        int securityScore = Math.max(0, 100 - countFindings(findings, FindingCategory.SECURITY) * 35);
        int performanceScore = Math.max(0, 100 - countFindings(findings, FindingCategory.PERFORMANCE) * 20 - (maxLoopDepth >= 3 ? 30 : (maxLoopDepth == 2 ? 15 : 0)) - (hasInputSizeAlloc ? 10 : 0));

        Map<String, Object> metricsMap = new HashMap<>();
        metricsMap.put("cyclomaticComplexity", totalComplexity);
        metricsMap.put("maxNestingDepth", maxNestingDepth);
        metricsMap.put("maxLoopNesting", maxLoopDepth);
        metricsMap.put("hasRecursion", hasRecursion);
        metricsMap.put("hasInputSizeAllocation", hasInputSizeAlloc);
        metricsMap.put("classCount", cu.findAll(ClassOrInterfaceDeclaration.class).size());
        metricsMap.put("methodCount", cu.findAll(MethodDeclaration.class).size());

        return AnalysisResult.builder()
                .language("JAVA")
                .syntaxStatus(SyntaxStatus.VALID)
                .syntaxValid(true)
                .findings(findings)
                .metrics(metricsMap)
                .cyclomaticComplexity(totalComplexity)
                .maxNestingDepth(maxNestingDepth)
                .estimatedTimeComplexity(timeComplexity)
                .estimatedSpaceComplexity(spaceComplexity)
                .hasInputSizeAllocation(hasInputSizeAlloc)
                .qualityScore(qualityScore)
                .complexityScore(complexityScore)
                .maintainabilityScore(maintainabilityScore)
                .securityScore(securityScore)
                .performanceScore(performanceScore)
                .codeAwareSuggestions(testingAnalysis.suggestions())
                .detectedInputTypes(testingAnalysis.detectedTypes())
                .coveredCases(testingAnalysis.coveredCases())
                .missingCases(testingAnalysis.missingCases())
                .evidenceTestingScore(testingAnalysis.evidenceTestingScore())
                .hasAssertions(testingAnalysis.hasAssertions())
                .testingApplicable(testingAnalysis.testingApplicable())
                .testingCategory(testingAnalysis.testingCategory())
                .build();
    }

    private CompilationUnit parseJavaCode(String code) {
        try {
            return StaticJavaParser.parse(code);
        } catch (ParseProblemException e) {
            if (!code.contains("class ")) {
                String wrapped = "public class Solution {\n" + code + "\n}";
                return StaticJavaParser.parse(wrapped);
            }
            throw e;
        }
    }

    private String estimateTimeComplexity(int loopDepth, boolean hasRecursion) {
        if (hasRecursion) {
            return loopDepth > 0 ? "O(2^N) or O(N log N)" : "O(N) (Recursive)";
        }
        return switch (loopDepth) {
            case 0 -> "O(1)";
            case 1 -> "O(N)";
            case 2 -> "O(N^2)";
            case 3 -> "O(N^3)";
            default -> "O(N^" + loopDepth + ")";
        };
    }

    private boolean detectInputSizeAllocation(CompilationUnit cu) {
        List<ArrayCreationExpr> arrayCreations = cu.findAll(ArrayCreationExpr.class);
        for (ArrayCreationExpr ace : arrayCreations) {
            for (ArrayCreationLevel level : ace.getLevels()) {
                if (level.getDimension().isPresent()) {
                    Expression dim = level.getDimension().get();
                    if (!dim.isIntegerLiteralExpr()) {
                        return true;
                    }
                }
            }
        }

        List<ObjectCreationExpr> objectCreations = cu.findAll(ObjectCreationExpr.class);
        for (ObjectCreationExpr oce : objectCreations) {
            String type = oce.getTypeAsString();
            if (type.contains("List") || type.contains("Map") || type.contains("Set") || type.contains("Vector")) {
                if (!oce.getArguments().isEmpty()) {
                    Expression arg0 = oce.getArguments().get(0);
                    if (!arg0.isIntegerLiteralExpr()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private int countFindings(List<FindingDto> findings, FindingCategory category) {
        return (int) findings.stream().filter(f -> f.getCategory() == category).count();
    }

    // AST Visitor for Cyclomatic Complexity & Nesting
    private static class ComplexityVisitor extends VoidVisitorAdapter<Void> {
        private int cyclomaticComplexity = 1;
        private int currentNesting = 0;
        private int maxNestingDepth = 0;
        private int currentLoopDepth = 0;
        private int maxLoopDepth = 0;
        private boolean hasRecursion = false;
        private String currentMethodName = null;

        public int getCyclomaticComplexity() { return cyclomaticComplexity; }
        public int getMaxNestingDepth() { return maxNestingDepth; }
        public int getMaxLoopDepth() { return maxLoopDepth; }
        public boolean isHasRecursion() { return hasRecursion; }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            String prevMethod = currentMethodName;
            currentMethodName = n.getNameAsString();
            super.visit(n, arg);
            currentMethodName = prevMethod;
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            if (currentMethodName != null && n.getNameAsString().equals(currentMethodName)) {
                hasRecursion = true;
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(IfStmt n, Void arg) {
            cyclomaticComplexity++;
            enterNesting();
            super.visit(n, arg);
            exitNesting();
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            cyclomaticComplexity++;
            enterNesting();
            enterLoop();
            super.visit(n, arg);
            exitLoop();
            exitNesting();
        }

        @Override
        public void visit(ForEachStmt n, Void arg) {
            cyclomaticComplexity++;
            enterNesting();
            enterLoop();
            super.visit(n, arg);
            exitLoop();
            exitNesting();
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            cyclomaticComplexity++;
            enterNesting();
            enterLoop();
            super.visit(n, arg);
            exitLoop();
            exitNesting();
        }

        @Override
        public void visit(DoStmt n, Void arg) {
            cyclomaticComplexity++;
            enterNesting();
            enterLoop();
            super.visit(n, arg);
            exitLoop();
            exitNesting();
        }

        @Override
        public void visit(CatchClause n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(ConditionalExpr n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        private void enterNesting() {
            currentNesting++;
            if (currentNesting > maxNestingDepth) {
                maxNestingDepth = currentNesting;
            }
        }

        private void exitNesting() {
            currentNesting--;
        }

        private void enterLoop() {
            currentLoopDepth++;
            if (currentLoopDepth > maxLoopDepth) {
                maxLoopDepth = currentLoopDepth;
            }
        }

        private void exitLoop() {
            currentLoopDepth--;
        }
    }

    // AST Visitor for Rule Checks (Quality, Security, Correctness, Performance)
    private static class AstRuleVisitor extends VoidVisitorAdapter<Void> {
        private final List<FindingDto> findings;
        private final String rawCode;

        public AstRuleVisitor(List<FindingDto> findings, String rawCode) {
            this.findings = findings;
            this.rawCode = rawCode;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            String name = n.getNameAsString();
            if (!Character.isUpperCase(name.charAt(0))) {
                addFinding(n, "JAVA_STYLE_CLASS_NAME", FindingSeverity.LOW, FindingCategory.CODE_QUALITY,
                        "Class Naming Convention Violation",
                        "Class name '" + name + "' should start with an uppercase letter (PascalCase).",
                        "Following standard Java naming conventions improves code readability.",
                        "Standard Java naming conventions improve readability. Consider naming classes using PascalCase.",
                        "Class names in Java should be nouns in PascalCase.");
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(CatchClause n, Void arg) {
            BlockStmt body = n.getBody();
            if (body.getStatements().isEmpty()) {
                addFinding(n, "JAVA_EMPTY_CATCH", FindingSeverity.HIGH, FindingCategory.CORRECTNESS,
                        "Empty Catch Block Detected",
                        "An exception is caught but silently ignored without logging or handling.",
                        "Swallowing exceptions obscures failures and makes debugging extremely difficult.",
                        "Swallowing exceptions obscures failures. Consider logging the error or propagating a domain-specific exception to maintain transparency.",
                        "Never leave catch blocks empty; at minimum log or rethrow.");
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(VariableDeclarator n, Void arg) {
            String name = n.getNameAsString();
            Optional<Expression> init = n.getInitializer();

            // Zero initialization pitfall check (e.g., int max = 0; or int min = 0;)
            if (init.isPresent() && init.get().isIntegerLiteralExpr()) {
                int val = init.get().asIntegerLiteralExpr().asInt();
                if (val == 0 && (name.toLowerCase().contains("max") || name.toLowerCase().contains("highest"))) {
                    addFinding(n, "JAVA_ZERO_INIT_MAX_PITFALL", FindingSeverity.HIGH, FindingCategory.CORRECTNESS,
                            "Potential Initialization Issue for Maximum Value",
                            "Your current initialization of '" + name + " = 0' assumes that the input contains non-negative values.",
                            "If the input contains only negative values, the method will return 0 instead of the true maximum element.",
                            "This initialization assumes a non-negative value range. Consider whether the initial value remains correct when the input contains negative or boundary values.",
                            "Ask yourself what happens when every element in the input is smaller than zero.");
                }
            }

            // Hardcoded secret check
            if (init.isPresent() && init.get().isStringLiteralExpr()) {
                String strVal = init.get().asStringLiteralExpr().getValue();
                if ((name.toLowerCase().contains("password") || name.toLowerCase().contains("secret") || name.toLowerCase().contains("apikey")) && !strVal.isEmpty()) {
                    addFinding(n, "JAVA_HARDCODED_SECRET_DETECTED", FindingSeverity.HIGH, FindingCategory.SECURITY,
                            "Hardcoded Credential or Sensitive Secret",
                            "Variable '" + name + "' appears to hold a hardcoded credential string.",
                            "Storing secrets directly in code risks exposing credentials in repositories.",
                            "Sensitive configuration should not live directly in source code. Consider moving this value into a protected configuration mechanism rather than storing it in the class.",
                            "Avoid embedding plain-text passwords or secret keys in source files.");
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            checkStringConcatInLoop(n);
            super.visit(n, arg);
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            checkStringConcatInLoop(n);
            super.visit(n, arg);
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            String name = n.getNameAsString();

            // PrintStackTrace check
            if (name.equals("printStackTrace")) {
                addFinding(n, "JAVA_PRINT_STACK_TRACE", FindingSeverity.LOW, FindingCategory.MAINTAINABILITY,
                        "Use of printStackTrace()",
                        "Calling printStackTrace() directly prints error output to standard error streams.",
                        "Production applications should use standard logging frameworks (e.g., SLF4J / Logger).",
                        "Direct stream output bypasses standard application logging. Consider routing diagnostic messages through a structured logging facade.",
                        "Use a logger like log.error(...) instead of printStackTrace().");
            }

            // Unsafe System.exit check
            if (name.equals("exit") && n.getScope().map(s -> s.toString().equals("System")).orElse(false)) {
                addFinding(n, "JAVA_UNSAFE_SYSTEM_EXIT", FindingSeverity.HIGH, FindingCategory.SECURITY,
                        "Unsafe System.exit() Call",
                        "Invoking System.exit() forcibly terminates the JVM host process.",
                        "Calling System.exit in library or web application code crashes the application container.",
                        "Terminating the JVM process affects the host application container. Consider throwing an exception or returning an error status instead.",
                        "Do not terminate the JVM in application code.");
            }

            super.visit(n, arg);
        }

        private void checkStringConcatInLoop(Statement loopStmt) {
            loopStmt.findAll(BinaryExpr.class).forEach(binaryExpr -> {
                if (binaryExpr.getOperator() == BinaryExpr.Operator.PLUS) {
                    // Simple heuristic check if inside loop string addition occurs
                    addFinding(binaryExpr, "JAVA_STRING_CONCAT_IN_LOOP", FindingSeverity.MEDIUM, FindingCategory.PERFORMANCE,
                            "Inefficient String Concatenation inside Loop",
                            "Using the '+' operator for String concatenation inside loops creates temporary String objects repeatedly.",
                            "String immutability leads to O(N^2) time complexity and excess memory allocation.",
                            "Repeated string creation inside a loop leads to frequent temporary allocations. Consider using an efficient string builder or accumulator designed for loop aggregation.",
                            "Consider replacing '+' with StringBuilder.append() inside loop bodies.");
                }
            });
        }

        private void addFinding(Node node, String ruleId, FindingSeverity severity, FindingCategory category,
                                String title, String message, String explanation, String recommendation, String hint) {

            int lineStart = node.getBegin().map(p -> p.line).orElse(1);
            int lineEnd = node.getEnd().map(p -> p.line).orElse(lineStart);
            int colStart = node.getBegin().map(p -> p.column).orElse(1);
            int colEnd = node.getEnd().map(p -> p.column).orElse(1);

            String snippet = extractSnippet(lineStart, lineEnd);

            // Avoid duplicate findings on same line for same rule
            boolean exists = findings.stream().anyMatch(f -> f.getRuleId().equals(ruleId) && Objects.equals(f.getLineStart(), lineStart));
            if (!exists) {
                findings.add(FindingDto.builder()
                        .ruleId(ruleId)
                        .severity(severity)
                        .category(category)
                        .title(title)
                        .message(message)
                        .explanation(explanation)
                        .recommendation(recommendation)
                        .hint(hint)
                        .lineStart(lineStart)
                        .lineEnd(lineEnd)
                        .columnStart(colStart)
                        .columnEnd(colEnd)
                        .codeSnippet(snippet)
                        .confidence(0.95)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }
        }

        private String extractSnippet(int startLine, int endLine) {
            String[] lines = rawCode.split("\r?\n");
            if (startLine <= lines.length && startLine > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = startLine - 1; i < Math.min(endLine, lines.length); i++) {
                    sb.append(lines[i].trim()).append(" ");
                }
                return sb.toString().trim();
            }
            return "";
        }
    }

    public CodeAwareTestingAnalysis analyzeInputStructures(CompilationUnit cu) {
        Set<String> inputTypes = new LinkedHashSet<>();
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

        boolean hasMainMethod = false;
        boolean mainUsesArgs = false;
        boolean hasBusinessMethods = false;
        boolean hasTestMethods = false;
        boolean hasAssertions = false;

        // 1. Check Assertions & Test Statements
        if (!cu.findAll(AssertStmt.class).isEmpty()) {
            hasAssertions = true;
        }
        List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);
        for (MethodCallExpr mc : methodCalls) {
            String name = mc.getNameAsString();
            if (name.startsWith("assert") || name.equals("fail") || name.startsWith("assertEquals") || name.startsWith("assertTrue")) {
                hasAssertions = true;
                break;
            }
        }

        // 2. Analyze Methods & Parameter Usage
        for (MethodDeclaration method : methods) {
            String methodName = method.getNameAsString();
            boolean isMain = methodName.equals("main") && method.isStatic() && method.isPublic();
            
            if (isMain) {
                hasMainMethod = true;
                if (!method.getParameters().isEmpty()) {
                    Parameter mainParam = method.getParameters().get(0);
                    String argsName = mainParam.getNameAsString();
                    
                    List<NameExpr> nameExprs = method.findAll(NameExpr.class);
                    for (NameExpr ne : nameExprs) {
                        if (ne.getNameAsString().equals(argsName)) {
                            mainUsesArgs = true;
                            break;
                        }
                    }
                }
            }

            boolean isTestMethod = method.getAnnotationByName("Test").isPresent() || methodName.toLowerCase().startsWith("test");
            if (isTestMethod) {
                hasTestMethods = true;
            }

            if (!isMain && !isTestMethod) {
                hasBusinessMethods = true;
            }

            // Check parameters for non-main methods (or main if mainUsesArgs)
            if (!isMain || mainUsesArgs) {
                for (Parameter param : method.getParameters()) {
                    String typeName = param.getTypeAsString();
                    String paramName = param.getNameAsString().toLowerCase();

                    if (isMain && mainUsesArgs && (typeName.contains("[]") || typeName.startsWith("String"))) {
                        inputTypes.add("CLI_ARGS");
                    } else if (typeName.contains("[][]") || typeName.startsWith("List<List") || typeName.toLowerCase().contains("matrix")) {
                        inputTypes.add("MATRIX");
                    } else if (typeName.contains("[]") || typeName.startsWith("List") || typeName.startsWith("Collection")
                            || typeName.startsWith("Set") || typeName.startsWith("ArrayList") || typeName.startsWith("Vector")) {
                        inputTypes.add("ARRAY");
                    } else if (typeName.equals("String") || typeName.equals("char[]") || typeName.equals("CharSequence")
                            || typeName.equals("Character[]") || typeName.equals("StringBuilder")) {
                        inputTypes.add("STRING");
                    } else if (typeName.toLowerCase().contains("tree") || typeName.equals("Node") || typeName.equals("TreeNode")) {
                        inputTypes.add("TREE");
                    } else if (typeName.toLowerCase().contains("graph") || typeName.equals("GraphNode")
                            || (typeName.contains("List<List") && (paramName.contains("adj") || paramName.contains("graph")))) {
                        inputTypes.add("GRAPH");
                    } else if (typeName.equals("int") || typeName.equals("long") || typeName.equals("double")
                            || typeName.equals("float") || typeName.equals("Integer") || typeName.equals("Long")
                            || typeName.equals("Double") || typeName.equals("Float") || typeName.equals("short") || typeName.equals("byte")) {
                        inputTypes.add("NUMERIC");
                    }
                }
            }
        }

        // Structural check for explicit array indexing in business logic
        if (!cu.findAll(ArrayAccessExpr.class).isEmpty()) {
            if (hasBusinessMethods) {
                inputTypes.add("ARRAY");
            }
        }

        // 3. Classify Testing Applicability & Category
        boolean testingApplicable = true;
        String testingCategory = "TESTABLE_LOGIC";

        if (hasTestMethods || hasAssertions) {
            testingCategory = "TEST_HARNESS_PRESENT";
            testingApplicable = true;
        } else if (!hasBusinessMethods && hasMainMethod && !mainUsesArgs) {
            testingCategory = "FIXED_OUTPUT";
            testingApplicable = false;
        } else if (!hasBusinessMethods && !hasMainMethod && inputTypes.isEmpty()) {
            testingCategory = "NO_TESTABLE_LOGIC";
            testingApplicable = false;
        } else if (mainUsesArgs && !hasBusinessMethods) {
            testingCategory = "CLI_ARGS";
            testingApplicable = true;
        }

        // 4. Neutral non-applicable classification for fixed-output / non-parameterized programs
        if (!testingApplicable || "FIXED_OUTPUT".equals(testingCategory) || "NO_TESTABLE_LOGIC".equals(testingCategory)) {
            return new CodeAwareTestingAnalysis(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    100,
                    hasAssertions,
                    false,
                    testingCategory
            );
        }

        // 5. Covered Edge Cases Analysis for testable code
        Set<String> coveredCases = new LinkedHashSet<>();

        if (hasAssertions || hasTestMethods) {
            List<ArrayInitializerExpr> arrayInits = cu.findAll(ArrayInitializerExpr.class);
            for (ArrayInitializerExpr ai : arrayInits) {
                int size = ai.getValues().size();
                if (size == 0) coveredCases.add("ARRAY_EMPTY");
                else if (size == 1) coveredCases.add("ARRAY_SINGLE");
                else if (size > 1) {
                    Set<String> uniqueVals = new HashSet<>();
                    for (Expression val : ai.getValues()) uniqueVals.add(val.toString());
                    if (uniqueVals.size() < size) coveredCases.add("ARRAY_DUPLICATE");
                }
                for (Expression val : ai.getValues()) {
                    if (val.isUnaryExpr() && val.asUnaryExpr().getOperator() == UnaryExpr.Operator.MINUS) {
                        coveredCases.add("ARRAY_NEGATIVE");
                        coveredCases.add("NUMERIC_NEGATIVE");
                    }
                }
            }

            List<ArrayCreationExpr> arrayCreations = cu.findAll(ArrayCreationExpr.class);
            for (ArrayCreationExpr ace : arrayCreations) {
                if (ace.getInitializer().isPresent() && ace.getInitializer().get().getValues().isEmpty()) {
                    coveredCases.add("ARRAY_EMPTY");
                }
                for (ArrayCreationLevel level : ace.getLevels()) {
                    if (level.getDimension().isPresent()) {
                        Expression dim = level.getDimension().get();
                        if (dim.isIntegerLiteralExpr()) {
                            int len = dim.asIntegerLiteralExpr().asInt();
                            if (len == 0) coveredCases.add("ARRAY_EMPTY");
                            if (len == 1) coveredCases.add("ARRAY_SINGLE");
                            if (len >= 50) coveredCases.add("ARRAY_LARGE");
                        }
                    }
                }
            }

            List<StringLiteralExpr> stringLiterals = cu.findAll(StringLiteralExpr.class);
            for (StringLiteralExpr sle : stringLiterals) {
                String val = sle.getValue();
                if (val.isEmpty()) coveredCases.add("STRING_EMPTY");
                else if (val.length() == 1) coveredCases.add("STRING_SINGLE");
                else if (val.trim().isEmpty()) coveredCases.add("STRING_WHITESPACE");
                else if (hasRepeatedChars(val)) coveredCases.add("STRING_REPEATED");
            }

            List<IntegerLiteralExpr> intLiterals = cu.findAll(IntegerLiteralExpr.class);
            for (IntegerLiteralExpr ile : intLiterals) {
                if (ile.asInt() == 0) coveredCases.add("NUMERIC_ZERO");
            }
            List<FieldAccessExpr> fieldAccesses = cu.findAll(FieldAccessExpr.class);
            for (FieldAccessExpr fa : fieldAccesses) {
                String fName = fa.getNameAsString();
                if (fName.equals("MIN_VALUE") || fName.equals("MAX_VALUE")) {
                    coveredCases.add("NUMERIC_BOUNDS");
                }
            }
        }

        // Generate missing suggestions strictly per active input type
        List<String> missingSuggestions = new ArrayList<>();
        List<String> missingCases = new ArrayList<>();

        if (inputTypes.contains("CLI_ARGS")) {
            if (!coveredCases.contains("CLI_EMPTY")) {
                missingCases.add("CLI_EMPTY");
                missingSuggestions.add("• No command-line arguments (empty args)");
            }
            if (!coveredCases.contains("CLI_SINGLE")) {
                missingCases.add("CLI_SINGLE");
                missingSuggestions.add("• Single command-line argument");
            }
            if (!coveredCases.contains("CLI_MULTIPLE")) {
                missingCases.add("CLI_MULTIPLE");
                missingSuggestions.add("• Multiple command-line arguments");
            }
        }

        if (inputTypes.contains("ARRAY")) {
            if (!coveredCases.contains("ARRAY_EMPTY")) {
                missingCases.add("ARRAY_EMPTY");
                missingSuggestions.add("• Empty array ([])");
            }
            if (!coveredCases.contains("ARRAY_SINGLE")) {
                missingCases.add("ARRAY_SINGLE");
                missingSuggestions.add("• Single-element array ([x])");
            }
            if (!coveredCases.contains("ARRAY_DUPLICATE")) {
                missingCases.add("ARRAY_DUPLICATE");
                missingSuggestions.add("• Duplicate values in array");
            }
            if (!coveredCases.contains("ARRAY_NEGATIVE")) {
                missingCases.add("ARRAY_NEGATIVE");
                missingSuggestions.add("• Negative or boundary array values");
            }
            if (!coveredCases.contains("ARRAY_SORTED")) {
                missingCases.add("ARRAY_SORTED");
                missingSuggestions.add("• Sorted and reverse-sorted inputs");
            }
            if (!coveredCases.contains("ARRAY_LARGE")) {
                missingCases.add("ARRAY_LARGE");
                missingSuggestions.add("• Large input array");
            }
        }

        if (inputTypes.contains("STRING")) {
            if (!coveredCases.contains("STRING_EMPTY")) {
                missingCases.add("STRING_EMPTY");
                missingSuggestions.add("• Empty string (\"\")");
            }
            if (!coveredCases.contains("STRING_SINGLE")) {
                missingCases.add("STRING_SINGLE");
                missingSuggestions.add("• Single-character string (\"a\")");
            }
            if (!coveredCases.contains("STRING_REPEATED")) {
                missingCases.add("STRING_REPEATED");
                missingSuggestions.add("• Repeated characters (\"aaaa\")");
            }
            if (!coveredCases.contains("STRING_WHITESPACE")) {
                missingCases.add("STRING_WHITESPACE");
                missingSuggestions.add("• Whitespace and special characters");
            }
        }

        if (inputTypes.contains("NUMERIC")) {
            if (!coveredCases.contains("NUMERIC_ZERO")) {
                missingCases.add("NUMERIC_ZERO");
                missingSuggestions.add("• Zero (0)");
            }
            if (!coveredCases.contains("NUMERIC_NEGATIVE")) {
                missingCases.add("NUMERIC_NEGATIVE");
                missingSuggestions.add("• Negative values where valid");
            }
            if (!coveredCases.contains("NUMERIC_BOUNDS")) {
                missingCases.add("NUMERIC_BOUNDS");
                missingSuggestions.add("• Minimum and maximum boundary limits (e.g. Integer.MIN_VALUE)");
            }
        }

        // Calculate evidence-based testing score
        int evidenceScore = 50;
        if (hasMainMethod || hasTestMethods || hasAssertions) {
            if (hasMainMethod || hasTestMethods) evidenceScore += 15;
            if (hasAssertions) evidenceScore += 15;
            evidenceScore += Math.min(20, coveredCases.size() * 5);
        }
        evidenceScore = Math.min(100, Math.max(50, evidenceScore));

        return new CodeAwareTestingAnalysis(
                new ArrayList<>(inputTypes),
                missingSuggestions,
                new ArrayList<>(coveredCases),
                missingCases,
                evidenceScore,
                hasAssertions,
                true,
                testingCategory
        );
    }

    private static boolean hasRepeatedChars(String s) {
        if (s == null || s.length() < 2) return false;
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == first) return true;
        }
        return false;
    }

    public record CodeAwareTestingAnalysis(
            List<String> detectedTypes,
            List<String> suggestions,
            List<String> coveredCases,
            List<String> missingCases,
            int evidenceTestingScore,
            boolean hasAssertions,
            boolean testingApplicable,
            String testingCategory
    ) {}
}
