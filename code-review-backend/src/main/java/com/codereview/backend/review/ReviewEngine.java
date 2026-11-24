package com.codereview.backend.review;

import com.codereview.backend.ai.AIReviewAdapter;
import com.codereview.backend.ai.AIReviewRequest;
import com.codereview.backend.ai.StructuredAIResponse;
import com.codereview.backend.analysis.cpp.CppLanguageAnalyzer;
import com.codereview.backend.analysis.core.*;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.analysis.performance.PerformanceAnalysisResult;
import com.codereview.backend.analysis.performance.PerformanceAnalyzer;
import com.codereview.backend.entity.*;
import com.codereview.backend.execution.ExecutionEngine;
import com.codereview.backend.execution.ExecutionRequest;
import com.codereview.backend.execution.ExecutionResult;
import com.codereview.backend.execution.ValidationExecutionEngine;
import com.codereview.backend.service.StuckUserTracker;
import com.codereview.backend.util.LanguageValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewEngine {

    private final JavaLanguageAnalyzer javaLanguageAnalyzer;
    private final CppLanguageAnalyzer cppLanguageAnalyzer;
    private final ExecutionEngine executionEngine;
    private final AIReviewAdapter aiReviewAdapter;
    private final StuckUserTracker stuckUserTracker;
    private final PerformanceAnalyzer performanceAnalyzer;

    @Autowired
    public ReviewEngine(JavaLanguageAnalyzer javaLanguageAnalyzer,
                        CppLanguageAnalyzer cppLanguageAnalyzer,
                        ExecutionEngine executionEngine,
                        AIReviewAdapter aiReviewAdapter,
                        StuckUserTracker stuckUserTracker,
                        PerformanceAnalyzer performanceAnalyzer) {
        this.javaLanguageAnalyzer = javaLanguageAnalyzer;
        this.cppLanguageAnalyzer = cppLanguageAnalyzer;
        this.executionEngine = executionEngine;
        this.aiReviewAdapter = aiReviewAdapter;
        this.stuckUserTracker = stuckUserTracker;
        this.performanceAnalyzer = performanceAnalyzer != null ? performanceAnalyzer : new PerformanceAnalyzer();
    }

    public ReviewEngine(JavaLanguageAnalyzer javaLanguageAnalyzer,
                        ExecutionEngine executionEngine,
                        AIReviewAdapter aiReviewAdapter,
                        StuckUserTracker stuckUserTracker,
                        PerformanceAnalyzer performanceAnalyzer) {
        this(javaLanguageAnalyzer, null, executionEngine, aiReviewAdapter, stuckUserTracker, performanceAnalyzer);
    }

    public ReviewEngine(JavaLanguageAnalyzer javaLanguageAnalyzer,
                        ExecutionEngine executionEngine,
                        AIReviewAdapter aiReviewAdapter,
                        StuckUserTracker stuckUserTracker) {
        this(javaLanguageAnalyzer, null, executionEngine, aiReviewAdapter, stuckUserTracker, new PerformanceAnalyzer());
    }

    /**
     * Conducts a unified review session for problem submission or freeform code.
     */
    public ReviewSessionEntity createReviewSession(UserEntity user, ProblemEntity problem, SubmissionEntity submission, String code, String sessionType) {
        return createReviewSession(user, problem, submission, code, sessionType, "JAVA");
    }

    public ReviewSessionEntity createReviewSession(UserEntity user, ProblemEntity problem, SubmissionEntity submission, String code, String sessionType, String language) {
        String lang = LanguageValidator.validateAndNormalize(language);
        boolean isCpp = LanguageValidator.isCpp(lang);

        // 1. Run Static Code Analysis
        AnalysisResult astResult;
        if (isCpp) {
            if (cppLanguageAnalyzer != null) {
                astResult = cppLanguageAnalyzer.analyze(AnalysisRequest.builder()
                        .code(code)
                        .language("CPP")
                        .problemId(problem != null ? problem.getId() : null)
                        .problemDescription(problem != null ? problem.getDescription() : null)
                        .build());
            } else {
                astResult = AnalysisResult.builder()
                        .language("CPP")
                        .syntaxStatus(SyntaxStatus.UNAVAILABLE)
                        .syntaxValid(null)
                        .parsingErrorMessage(null)
                        .estimatedTimeComplexity("Unknown")
                        .estimatedSpaceComplexity("Unknown")
                        .qualityScore(0)
                        .complexityScore(0)
                        .maintainabilityScore(0)
                        .securityScore(0)
                        .performanceScore(0)
                        .findings(List.of(FindingDto.builder()
                                .ruleId("CPP_ANALYZER_UNAVAILABLE")
                                .severity(FindingSeverity.HIGH)
                                .category(FindingCategory.MAINTAINABILITY)
                                .title("C++ Analyzer Unavailable")
                                .message("C++ code analysis engine is not configured.")
                                .explanation("Static analysis for C++ is unavailable in this runtime configuration.")
                                .recommendation("Ensure C++ language analysis components are configured and enabled.")
                                .lineStart(1)
                                .lineEnd(1)
                                .confidence(1.0)
                                .source(FindingSource.DETERMINISTIC)
                                .build()))
                        .build();
            }
        } else {
            astResult = javaLanguageAnalyzer.analyze(AnalysisRequest.builder()
                    .code(code)
                    .language("JAVA")
                    .problemId(problem != null ? problem.getId() : null)
                    .problemDescription(problem != null ? problem.getDescription() : null)
                    .build());
        }

        List<FindingDto> findings = new ArrayList<>(astResult.getFindings());

        // 2. Evaluate Test Execution Correctness & Generate Actionable Testing Findings
        int correctnessScore = 100;
        int testingScore = 50;

        ExecutionResult execResult = null;
        String executionSummaryStr = "Free-form code review.";

        if (problem != null && problem.getTestCases() != null && !problem.getTestCases().isEmpty()) {
            execResult = executionEngine.execute(ExecutionRequest.builder()
                    .code(code)
                    .language(isCpp ? "CPP" : "JAVA")
                    .testCases(problem.getTestCases())
                    .build());

            if ("PASSED".equals(execResult.getStatus())) {
                correctnessScore = 100;
                testingScore = 100;
                executionSummaryStr = "All " + execResult.getTotalTestCases() + " test cases passed during evaluation.";
            } else if ("FAILED".equals(execResult.getStatus())) {
                int passed = execResult.getPassedTestCases();
                int total = execResult.getTotalTestCases();
                double ratio = (double) passed / Math.max(1, total);
                correctnessScore = (int) Math.round(ratio * 100.0);
                testingScore = correctnessScore;
                executionSummaryStr = passed + " of " + total + " test cases passed during evaluation.";

                findings.add(FindingDto.builder()
                        .ruleId((isCpp ? "CPP" : "JAVA") + "_CHALLENGE_TEST_SUITE_PARTIAL_FAILURE")
                        .severity(FindingSeverity.HIGH)
                        .category(FindingCategory.TESTING)
                        .title("Challenge Test Suite Partial Failure (" + passed + "/" + total + " Passed)")
                        .message("Submission passed " + passed + " out of " + total + " test cases during platform evaluation.")
                        .explanation("The solution failed " + (total - passed) + " test cases during automated execution. Certain boundary conditions or expected outputs were not met.")
                        .recommendation("Review boundary conditions (e.g. empty inputs, negative values, large bounds) to pass all test cases.")
                        .hint("Why it matters: Failing test cases indicate unhandled edge cases or logical flaws under specific input constraints.")
                        .confidence(1.0)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            } else { // COMPILATION_ERROR or RUNTIME_ERROR
                correctnessScore = 0;
                testingScore = 20;
                executionSummaryStr = "Test execution blocked due to status: " + execResult.getStatus();

                findings.add(FindingDto.builder()
                        .ruleId((isCpp ? "CPP" : "JAVA") + "_TEST_EXECUTION_BLOCKED")
                        .severity(FindingSeverity.HIGH)
                        .category(FindingCategory.TESTING)
                        .title("Test Execution Blocked (" + execResult.getStatus() + ")")
                        .message("Automated test execution could not complete due to compilation or execution failure.")
                        .explanation("The submitted code encountered errors during build/execution, preventing automated test cases from running to completion.")
                        .recommendation("Fix compilation syntax errors or runtime exceptions to enable test evaluation.")
                        .hint("Why it matters: Code must compile and execute cleanly before test cases can be evaluated.")
                        .confidence(1.0)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }
        } else {
            // FREE-FORM REVIEW context (no platform test suite)
            if (executionEngine instanceof ValidationExecutionEngine vee) {
                execResult = vee.executeFreeform(code, 2000L, isCpp ? "CPP" : "JAVA");
                if (execResult != null && execResult.getUserFacingSummary() != null) {
                    executionSummaryStr = execResult.getUserFacingSummary();
                }
            }

            if (astResult.getSyntaxStatus() == SyntaxStatus.INVALID) {
                correctnessScore = 0;
                testingScore = 20;
            } else {
                boolean testingApplicable = astResult.isTestingApplicable();
                String testingCategory = astResult.getTestingCategory();

                String contextKey = (user != null ? user.getId().toString() : "anon") + "_" + (problem != null && problem.getId() != null ? problem.getId().toString() : "freeform");

                if (!testingApplicable || "FIXED_OUTPUT".equals(testingCategory) || "NO_TESTABLE_LOGIC".equals(testingCategory) || "CONSOLE_IO".equals(testingCategory)) {
                    testingScore = 100;
                    stuckUserTracker.reset(contextKey);
                } else {
                    testingScore = astResult.getEvidenceTestingScore() != null ? astResult.getEvidenceTestingScore() : 50;

                    if (testingScore >= 90 && astResult.isHasAssertions()) {
                        stuckUserTracker.reset(contextKey);
                    } else {
                        String limitedRuleId = (isCpp ? "CPP" : "JAVA") + "_FREEFORM_TESTING_LIMITED";
                        int stuckCount = stuckUserTracker.getAndUpdateStuckCount(contextKey, code, limitedRuleId);
                        
                        List<String> suggestions = astResult.getCodeAwareSuggestions();
                        String suggestionsText = (suggestions != null && !suggestions.isEmpty())
                                ? String.join("\n", suggestions)
                                : "• Parameter boundary validation\n• Edge input handling";

                        String explanation = "Your implementation is analyzable, but the current code provides limited executable evidence for boundary behavior.";
                        if (!astResult.isHasAssertions()) {
                            explanation += " No explicit assertions (e.g. assert statement or assertEquals) were found to validate returned values.";
                        }

                        String hint = "Why it matters: Boundary inputs and unhandled edge cases are common sources of unexpected runtime exceptions.";
                        if (stuckCount >= 3) {
                            hint = generateGuidedCodeHint(astResult.getDetectedInputTypes());
                        }

                        findings.add(FindingDto.builder()
                                .ruleId(limitedRuleId)
                                .severity(FindingSeverity.MEDIUM)
                                .category(FindingCategory.TESTING)
                                .title("Testing Coverage Could Be Improved")
                                .message("Testing confidence is limited based on executable test evidence in the submitted code.")
                                .explanation(explanation)
                                .recommendation("Consider validating the following edge cases:\n" + suggestionsText)
                                .hint(hint)
                                .confidence(0.85)
                                .source(FindingSource.DETERMINISTIC)
                                .build());
                    }
                }
            }
        }

        // Sub-scores directly from AST analysis
        int qualityScore = astResult.getQualityScore() != null ? astResult.getQualityScore() : 0;
        int complexityScore = astResult.getComplexityScore() != null ? astResult.getComplexityScore() : 0;
        int maintainabilityScore = astResult.getMaintainabilityScore() != null ? astResult.getMaintainabilityScore() : 0;
        int securityScore = astResult.getSecurityScore() != null ? astResult.getSecurityScore() : 0;
        int performanceScore = astResult.getPerformanceScore() != null ? astResult.getPerformanceScore() : 0;

        // 2b. Run Deterministic Performance & Optimization Analyzer (Java specific AST)
        PerformanceAnalysisResult perfAnalysis = null;
        if (!isCpp) {
            perfAnalysis = performanceAnalyzer.analyze(astResult, code, problem);
            if (perfAnalysis != null && perfAnalysis.getFindings() != null) {
                findings.addAll(perfAnalysis.getFindings());
                if (perfAnalysis.isHasOptimizationOpportunity()) {
                    performanceScore = Math.max(30, performanceScore - 15);
                }
            }
        }

        // Problem-aware complexity and performance adjustments
        if (problem != null) {
            boolean inPlaceViolation = Boolean.TRUE.equals(problem.getInPlaceRequired()) && astResult.isHasInputSizeAllocation();
            
            if (inPlaceViolation) {
                complexityScore = Math.max(0, complexityScore - 45);
                performanceScore = Math.max(0, performanceScore - 40);
                qualityScore = Math.max(0, qualityScore - 20);

                findings.add(FindingDto.builder()
                        .ruleId((isCpp ? "CPP" : "JAVA") + "_INPLACE_REQUIRED_VIOLATION")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.COMPLEXITY)
                        .title("In-place Requirement Not Satisfied")
                        .message("The problem requires modifying elements in-place with O(1) auxiliary space, but an input-proportional allocation was detected.")
                        .explanation("Creating an additional array or data structure proportional to the input size increases memory overhead to O(N) when O(1) auxiliary space is required.")
                        .recommendation("Try rearranging the input elements directly in-place without allocating auxiliary arrays.")
                        .hint("Consider swapping elements from both ends toward the middle in-place.")
                        .confidence(0.9)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            } else if (problem.getExpectedSpaceComplexity() != null && "O(1)".equalsIgnoreCase(problem.getExpectedSpaceComplexity()) && astResult.isHasInputSizeAllocation()) {
                complexityScore = Math.max(0, complexityScore - 35);
                performanceScore = Math.max(0, performanceScore - 30);

                findings.add(FindingDto.builder()
                        .ruleId((isCpp ? "CPP" : "JAVA") + "_SPACE_COMPLEXITY_EXCEEDED")
                        .severity(FindingSeverity.MEDIUM)
                        .category(FindingCategory.COMPLEXITY)
                        .title("Auxiliary Space Exceeds Expected Complexity")
                        .message("Expected O(1) auxiliary space, but code allocates memory proportional to input size O(N).")
                        .explanation("Allocating extra data structures proportional to input size increases space complexity.")
                        .recommendation("Optimize space usage to work within O(1) auxiliary space.")
                        .confidence(0.85)
                        .source(FindingSource.DETERMINISTIC)
                        .build());
            }

            // Compare time complexity
            if (problem.getExpectedTimeComplexity() != null && astResult.getEstimatedTimeComplexity() != null) {
                String expectedTime = problem.getExpectedTimeComplexity().toUpperCase();
                String actualTime = astResult.getEstimatedTimeComplexity().toUpperCase();
                if ((expectedTime.contains("O(N)") || expectedTime.contains("O(1)")) && actualTime.contains("O(N^2)")) {
                    complexityScore = Math.max(0, complexityScore - 40);
                    performanceScore = Math.max(0, performanceScore - 45);

                    findings.add(FindingDto.builder()
                            .ruleId((isCpp ? "CPP" : "JAVA") + "_TIME_COMPLEXITY_EXCEEDED")
                            .severity(FindingSeverity.HIGH)
                            .category(FindingCategory.PERFORMANCE)
                            .title("Time Complexity Exceeds Expected Target")
                            .message("Expected " + problem.getExpectedTimeComplexity() + " time complexity, but detected nested loops leading to " + astResult.getEstimatedTimeComplexity() + ".")
                            .explanation("Nested loops significantly slow down execution for larger input sizes.")
                            .recommendation("Look for linear single-pass or hash-based lookup strategies to avoid nested iteration.")
                            .confidence(0.9)
                            .source(FindingSource.DETERMINISTIC)
                            .build());
                }
            }
        }

        // 3. AI Semantic Review Adapter Call
        StructuredAIResponse aiResponse = aiReviewAdapter.generateReview(AIReviewRequest.builder()
                .code(code)
                .language(isCpp ? "CPP" : "JAVA")
                .deterministicFindings(findings)
                .metrics(astResult.getMetrics())
                .estimatedTimeComplexity(perfAnalysis != null && perfAnalysis.getTimeComplexity() != null ? perfAnalysis.getTimeComplexity() : astResult.getEstimatedTimeComplexity())
                .estimatedSpaceComplexity(perfAnalysis != null && perfAnalysis.getSpaceComplexity() != null ? perfAnalysis.getSpaceComplexity() : astResult.getEstimatedSpaceComplexity())
                .problemTitle(problem != null ? problem.getTitle() : (isCpp ? "C++ Code Analysis" : "Free-form Review"))
                .problemDescription(problem != null ? problem.getDescription() : "")
                .problemConstraints(problem != null ? problem.getConstraints() : "")
                .codeAwareSuggestions(astResult.getCodeAwareSuggestions())
                .executionSummary(executionSummaryStr)
                .executionStatus(execResult != null ? execResult.getStatus() : "NOT_APPLICABLE")
                .executionStdout(execResult != null ? execResult.getStdout() : null)
                .executionStderr(execResult != null ? execResult.getStderr() : null)
                .executionTimeMs(execResult != null ? execResult.getExecutionTimeMs() : null)
                .executionApplicable(execResult != null ? execResult.isApplicable() : false)
                .isChallengeMode(problem != null)
                .build());


        // Process contextual findings from AI safely without removing/overwriting deterministic facts
        if (aiResponse.getContextualFindings() != null && !aiResponse.getContextualFindings().isEmpty()) {
            for (StructuredAIResponse.ContextualFindingItem cf : aiResponse.getContextualFindings()) {
                if (cf == null || cf.getTitle() == null) continue;

                String cfTitleLower = cf.getTitle().toLowerCase();

                Optional<FindingDto> existingMatch = findings.stream()
                        .filter(f -> f.getTitle() != null && (
                                f.getTitle().toLowerCase().contains(cfTitleLower) ||
                                cfTitleLower.contains(f.getTitle().toLowerCase()) ||
                                (cfTitleLower.contains("secret") && f.getTitle().toLowerCase().contains("secret")) ||
                                (cfTitleLower.contains("place") && f.getTitle().toLowerCase().contains("place")) ||
                                (cfTitleLower.contains("complexity") && f.getTitle().toLowerCase().contains("complexity"))
                        ))
                        .findFirst();

                if (existingMatch.isPresent()) {
                    FindingDto match = existingMatch.get();
                    if (cf.getWhyItMatters() != null && !cf.getWhyItMatters().isEmpty()) {
                        String currentExp = match.getExplanation() != null ? match.getExplanation() : "";
                        if (!currentExp.contains(cf.getWhyItMatters())) {
                            match.setExplanation(currentExp + " Contextual AI note: " + cf.getWhyItMatters());
                        }
                    }
                    if (cf.getGuidance() != null && !cf.getGuidance().isEmpty()) {
                        String currentRec = match.getRecommendation() != null ? match.getRecommendation() : "";
                        if (!currentRec.contains(cf.getGuidance())) {
                            match.setRecommendation(currentRec + " Hint: " + cf.getGuidance());
                        }
                    }
                } else {
                    FindingSeverity severity = FindingSeverity.LOW;
                    if (cf.getSeverity() != null) {
                        try {
                            severity = FindingSeverity.valueOf(cf.getSeverity().toUpperCase());
                        } catch (IllegalArgumentException ignored) {}
                    }

                    findings.add(FindingDto.builder()
                            .ruleId("AI_CONTEXTUAL_" + Math.abs(cf.getTitle().hashCode()))
                            .severity(severity)
                            .category(FindingCategory.CODE_QUALITY)
                            .title(cf.getTitle())
                            .message(cf.getExplanation() != null ? cf.getExplanation() : cf.getTitle())
                            .explanation(cf.getWhyItMatters() != null ? cf.getWhyItMatters() : cf.getExplanation())
                            .recommendation(cf.getGuidance())
                            .confidence(0.85)
                            .source(FindingSource.AI)
                            .build());
                }
            }
        }

        // 4. Calculate Transparent Weighted Overall Score (0-100)
        // Weighted formula: Correctness (30%) + Quality (20%) + Complexity (15%) + Maintainability (15%) + Performance (10%) + Testing (10%)
        int overallScore = (int) Math.round(
                (correctnessScore * 0.30) +
                (qualityScore * 0.20) +
                (complexityScore * 0.15) +
                (maintainabilityScore * 0.15) +
                (performanceScore * 0.10) +
                (testingScore * 0.10)
        );
        overallScore = Math.max(0, Math.min(100, overallScore));

        // Format summary with strengths and tradeoffs
        StringBuilder summaryBuilder = new StringBuilder();
        if (aiResponse.getSummary() != null && !aiResponse.getSummary().isEmpty()) {
            summaryBuilder.append(aiResponse.getSummary());
        }

        if (aiResponse.getStrengths() != null && !aiResponse.getStrengths().isEmpty()) {
            summaryBuilder.append("\n\nKey Strengths:\n• ").append(String.join("\n• ", aiResponse.getStrengths()));
        }

        if (aiResponse.getTradeoffs() != null && !aiResponse.getTradeoffs().isEmpty()) {
            summaryBuilder.append("\n\nTradeoffs & Considerations:\n• ").append(String.join("\n• ", aiResponse.getTradeoffs()));
        }

        String finalSummary = summaryBuilder.toString().trim();

        // 5. Assemble ReviewSessionEntity
        ReviewSessionEntity session = ReviewSessionEntity.builder()
                .user(user)
                .problem(problem)
                .submission(submission)
                .code(code)
                .sessionType(sessionType)
                .language(isCpp ? "CPP" : "JAVA")
                .overallScore(overallScore)
                .correctnessScore(correctnessScore)
                .qualityScore(qualityScore)
                .complexityScore(complexityScore)
                .maintainabilityScore(maintainabilityScore)
                .securityScore(securityScore)
                .performanceScore(performanceScore)
                .testingScore(testingScore)
                .timeComplexity(perfAnalysis != null && perfAnalysis.getTimeComplexity() != null ? perfAnalysis.getTimeComplexity() : astResult.getEstimatedTimeComplexity())
                .spaceComplexity(perfAnalysis != null && perfAnalysis.getSpaceComplexity() != null ? perfAnalysis.getSpaceComplexity() : astResult.getEstimatedSpaceComplexity())
                .syntaxStatus(astResult.getSyntaxStatus() != null ? astResult.getSyntaxStatus().name() : "VALID")
                .executionStatus(execResult != null ? execResult.getStatus() : "NOT_APPLICABLE")
                .executionStdout(execResult != null ? execResult.getStdout() : null)
                .executionStderr(execResult != null ? execResult.getStderr() : null)
                .executionTimeMs(execResult != null ? execResult.getExecutionTimeMs() : null)
                .executionApplicable(execResult != null ? execResult.isApplicable() : false)
                .executionSummary(execResult != null ? execResult.getUserFacingSummary() : "Execution not applicable.")
                .summary(finalSummary)
                .aiProvider(aiResponse.getAiProvider())
                .aiModel(aiResponse.getAiModel())
                .aiKeySource(aiResponse.getAiKeySource())
                .findings(new ArrayList<>())
                .metrics(new ArrayList<>())
                .testSuggestions(new ArrayList<>())
                .build();


        // Attach Findings
        findings.forEach(f -> {
            FindingEntity entity = FindingEntity.builder()
                    .reviewSession(session)
                    .ruleId(f.getRuleId())
                    .severity(f.getSeverity().name())
                    .category(f.getCategory().name())
                    .title(f.getTitle())
                    .message(f.getMessage())
                    .explanation(f.getExplanation())
                    .recommendation(f.getRecommendation())
                    .hint(f.getHint())
                    .lineStart(f.getLineStart())
                    .lineEnd(f.getLineEnd())
                    .columnStart(f.getColumnStart())
                    .columnEnd(f.getColumnEnd())
                    .codeSnippet(f.getCodeSnippet())
                    .confidence(f.getConfidence())
                    .source(f.getSource().name())
                    .build();
            session.getFindings().add(entity);
        });

        // Attach Metrics
        if (astResult.getCyclomaticComplexity() != null) {
            session.getMetrics().add(ReviewMetricEntity.builder()
                    .reviewSession(session)
                    .metricKey("CYCLOMATIC_COMPLEXITY")
                    .metricName("Cyclomatic Complexity")
                    .metricValue(String.valueOf(astResult.getCyclomaticComplexity()))
                    .metricCategory("COMPLEXITY")
                    .description("Number of linearly independent paths through code")
                    .build());
        }

        if (astResult.getMaxNestingDepth() != null) {
            session.getMetrics().add(ReviewMetricEntity.builder()
                    .reviewSession(session)
                    .metricKey("MAX_NESTING_DEPTH")
                    .metricName("Maximum Nesting Depth")
                    .metricValue(String.valueOf(astResult.getMaxNestingDepth()))
                    .metricCategory("COMPLEXITY")
                    .description("Deepest nesting level of control structures")
                    .build());
        }

        // Attach AI Edge Case / Test Suggestions
        aiResponse.getTestSuggestions().forEach(ts -> {
            TestSuggestionEntity entity = TestSuggestionEntity.builder()
                    .reviewSession(session)
                    .category(ts.getCategory())
                    .description(ts.getDescription())
                    .sampleInput(ts.getSampleInput())
                    .expectedBehavior(ts.getExpectedBehavior())
                    .build();
            session.getTestSuggestions().add(entity);
        });

        return session;
    }

    private String generateGuidedCodeHint(List<String> detectedTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("// Guided Code Hint (Attempt 3+)\n");
        sb.append("// Use explicit assertions to validate boundary scenarios:\n");
        if (detectedTypes != null && detectedTypes.contains("ARRAY")) {
            sb.append("int[] emptyInput = {};\n");
            sb.append("int[] actual = yourMethod(emptyInput);\n");
            sb.append("assert actual != null && actual.length == 0;");
        } else if (detectedTypes != null && detectedTypes.contains("STRING")) {
            sb.append("String emptyInput = \"\";\n");
            sb.append("String actual = yourMethod(emptyInput);\n");
            sb.append("assert actual != null && actual.isEmpty();");
        } else {
            sb.append("int actual = yourMethod(0);\n");
            sb.append("assert actual == 0;");
        }
        return sb.toString();
    }
}
