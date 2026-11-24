package com.codereview.backend.execution.cpp;

import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.analysis.cpp.CppLanguageAnalyzer;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.analysis.performance.PerformanceAnalyzer;
import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.entity.TestCaseEntity;
import com.codereview.backend.execution.ExecutionRequest;
import com.codereview.backend.execution.ExecutionResult;
import com.codereview.backend.execution.TestCaseExecutionDetail;
import com.codereview.backend.execution.ValidationExecutionEngine;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.service.StuckUserTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CppExecutionAndCompilationTest {

    private CppCompilerService compilerService;
    private CppExecutionEngine cppExecutionEngine;
    private CppLanguageAnalyzer cppAnalyzer;
    private ValidationExecutionEngine validationEngine;
    private ReviewEngine reviewEngine;

    @BeforeEach
    void setUp() {
        compilerService = new CppCompilerService("", "");
        cppExecutionEngine = new CppExecutionEngine(compilerService);
        cppAnalyzer = new CppLanguageAnalyzer(compilerService);

        JavaLanguageAnalyzer javaAnalyzer = new JavaLanguageAnalyzer();
        validationEngine = new ValidationExecutionEngine(javaAnalyzer, cppExecutionEngine);

        DeterministicAIReviewAdapter aiAdapter = new DeterministicAIReviewAdapter();
        StuckUserTracker stuckTracker = new StuckUserTracker();

        PerformanceAnalyzer perfAnalyzer = new PerformanceAnalyzer();
        reviewEngine = new ReviewEngine(javaAnalyzer, cppAnalyzer, validationEngine, aiAdapter, stuckTracker, perfAnalyzer);
    }

    // ── C++ COMPILATION TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("C++ Compilation: Valid C++ program")
    void testValidCppProgramCompilation() {
        String code = """
                #include <iostream>
                int main() {
                    std::cout << "Compile OK" << std::endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertNotNull(result);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("Compile OK"));
    }

    @Test
    @DisplayName("C++ Compilation: Syntax error detected")
    void testSyntaxErrorCompilation() {
        String code = """
                #include <iostream>
                int main() {
                    int a = ; // syntax error
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertNotNull(result);
        assertEquals("COMPILATION_ERROR", result.getStatus());
        assertNotNull(result.getCompilationOutput());
        assertFalse(result.getCompilationOutput().isEmpty());
    }

    @Test
    @DisplayName("C++ Compilation: Missing entry point (no main)")
    void testMissingEntryPoint() {
        String code = """
                #include <iostream>
                void helper() {
                    std::cout << "No main function here" << std::endl;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertNotNull(result);
        assertEquals("NOT_APPLICABLE", result.getStatus());
        assertFalse(result.isApplicable());
        assertTrue(result.getUserFacingSummary().contains("No runnable main entry point found"));
    }

    @Test
    @DisplayName("C++ Compilation: Invalid / Empty source input")
    void testInvalidSourceInput() {
        ExecutionResult resultEmpty = cppExecutionEngine.executeFreeform("", 3000L);
        assertEquals("COMPILATION_ERROR", resultEmpty.getStatus());
        assertTrue(resultEmpty.getUserFacingSummary().contains("empty"));

        ExecutionResult resultNull = cppExecutionEngine.executeFreeform(null, 3000L);
        assertEquals("COMPILATION_ERROR", resultNull.getStatus());
    }

    // ── C++ EXECUTION TESTS ────────────────────────────────────────────────────

    @Test
    @DisplayName("C++ Execution: Hello World output capture")
    void testHelloWorldExecution() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    cout << "Hello, C++ World!" << endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("Hello, C++ World!"));
        assertNotNull(result.getExecutionTimeMs());
        assertTrue(result.getExecutionTimeMs() > 0);
    }

    @Test
    @DisplayName("C++ Execution: Arithmetic operations")
    void testArithmeticOperations() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int a = 15;
                    int b = 27;
                    int sum = a + b;
                    int product = a * b;
                    cout << "Sum: " << sum << ", Product: " << product << endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("Sum: 42, Product: 405"));
    }

    @Test
    @DisplayName("C++ Execution: Loops and conditionals")
    void testLoopsAndConditionals() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int sumEven = 0;
                    for (int i = 1; i <= 10; i++) {
                        if (i % 2 == 0) {
                            sumEven += i;
                        }
                    }
                    cout << "SumEven: " << sumEven << endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("SumEven: 30"));
    }

    @Test
    @DisplayName("C++ Execution: Functions and recursion")
    void testFunctionsAndRecursion() {
        String code = """
                #include <iostream>
                using namespace std;
                int factorial(int n) {
                    if (n <= 1) return 1;
                    return n * factorial(n - 1);
                }
                int main() {
                    cout << "Fact(5): " << factorial(5) << endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("Fact(5): 120"));
    }

    @Test
    @DisplayName("C++ Execution: Arrays and strings")
    void testArraysAndStrings() {
        String code = """
                #include <iostream>
                #include <vector>
                #include <string>
                #include <algorithm>
                using namespace std;
                int main() {
                    vector<int> nums = {5, 2, 8, 1, 9};
                    sort(nums.begin(), nums.end());
                    string s = "assistant";
                    reverse(s.begin(), s.end());
                    cout << "SortedFirst: " << nums[0] << ", Reversed: " << s << endl;
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("PASSED", result.getStatus());
        assertTrue(result.getStdout().contains("SortedFirst: 1, Reversed: tnatsissa"));
    }

    @Test
    @DisplayName("C++ Execution: Standard input / output (cin/cout)")
    void testStandardInputOutput() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int a, b;
                    if (cin >> a >> b) {
                        cout << (a + b) << endl;
                    }
                    return 0;
                }
                """;

        List<TestCaseEntity> testCases = List.of(
                TestCaseEntity.builder().id(1L).input("14 28").expectedOutput("42").isHidden(false).build(),
                TestCaseEntity.builder().id(2L).input("100 250").expectedOutput("350").isHidden(true).build()
        );

        ExecutionResult result = cppExecutionEngine.execute(ExecutionRequest.builder()
                .code(code)
                .language("CPP")
                .testCases(testCases)
                .timeLimitMs(3000L)
                .build());

        assertEquals("PASSED", result.getStatus());
        assertEquals(2, result.getPassedTestCases());
        assertEquals(2, result.getTotalTestCases());

        // Verify hidden test case masking
        TestCaseExecutionDetail hidden = result.getTestCaseDetails().stream()
                .filter(TestCaseExecutionDetail::isHidden)
                .findFirst().orElseThrow();
        assertEquals("[Hidden Case]", hidden.getInput());
        assertEquals("[Hidden Case]", hidden.getExpectedOutput());
        assertEquals("[Hidden Case]", hidden.getActualOutput());
    }

    @Test
    @DisplayName("C++ Execution: Multiple test cases with partial failure")
    void testMultipleTestCasesPartialFailure() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int a;
                    if (cin >> a) {
                        // Correct for positive, wrong for negative
                        cout << (a > 0 ? a * 2 : 0) << endl;
                    }
                    return 0;
                }
                """;

        List<TestCaseEntity> testCases = List.of(
                TestCaseEntity.builder().id(1L).input("5").expectedOutput("10").isHidden(false).build(),
                TestCaseEntity.builder().id(2L).input("-3").expectedOutput("-6").isHidden(false).build()
        );

        ExecutionResult result = cppExecutionEngine.execute(ExecutionRequest.builder()
                .code(code)
                .language("CPP")
                .testCases(testCases)
                .timeLimitMs(3000L)
                .build());

        assertEquals("FAILED", result.getStatus());
        assertEquals(1, result.getPassedTestCases());
        assertEquals(2, result.getTotalTestCases());
    }

    @Test
    @DisplayName("C++ Execution: Runtime failure (non-zero exit code)")
    void testRuntimeFailure() {
        String code = """
                #include <cstdlib>
                int main() {
                    exit(1); // non-zero exit code
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 3000L);
        assertEquals("RUNTIME_ERROR", result.getStatus());
        assertTrue(result.getUserFacingSummary().contains("Runtime Error"));
    }

    @Test
    @DisplayName("C++ Execution: Execution timeout / infinite loop")
    void testExecutionTimeout() {
        String code = """
                int main() {
                    while (true) {
                        // Infinite loop
                    }
                    return 0;
                }
                """;

        ExecutionResult result = cppExecutionEngine.executeFreeform(code, 1000L);
        assertEquals("TIME_LIMIT_EXCEEDED", result.getStatus());
        assertTrue(result.isTimedOut());
        assertTrue(result.getUserFacingSummary().contains("Time Limit Exceeded"));
    }

    // ── INTEGRATION & AI REVIEW TESTS FOR C++ ─────────────────────────────────

    @Test
    @DisplayName("C++ Freeform Review: Full ReviewEngine integration")
    void testCppFreeformReviewEngineIntegration() {
        String code = """
                #include <iostream>
                #include <vector>
                using namespace std;

                // Function with pass-by-value vector
                void printValues(vector<int> v) {
                    for (int x : v) {
                        cout << x << " ";
                    }
                    cout << endl;
                }

                int main() {
                    vector<int> numbers = {1, 2, 3};
                    printValues(numbers);
                    return 0;
                }
                """;

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, null, null, code, "FREEFORM_REVIEW", "CPP");

        assertNotNull(session);
        assertEquals("CPP", session.getLanguage());
        assertEquals("PASSED", session.getExecutionStatus());
        assertNotNull(session.getExecutionStdout());
        assertTrue(session.getExecutionStdout().contains("1 2 3"));

        // Check C++ specific finding detection: vector pass-by-value
        boolean hasPassByValueFinding = session.getFindings().stream()
                .anyMatch(f -> "CPP_CONTAINER_PASS_BY_VALUE".equals(f.getRuleId()));
        assertTrue(hasPassByValueFinding, "Should detect CPP_CONTAINER_PASS_BY_VALUE finding!");

        // AI review summary should be generated for C++
        assertNotNull(session.getSummary());
        assertTrue(session.getSummary().contains("C++"));
    }

    @Test
    @DisplayName("C++ Problem Submission: Full ReviewEngine integration")
    void testCppProblemSubmissionReviewEngine() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int n;
                    if (cin >> n) {
                        cout << (n * n) << endl;
                    }
                    return 0;
                }
                """;

        ProblemEntity problem = ProblemEntity.builder()
                .id(99L)
                .title("Square Number")
                .description("Given n, return n * n.")
                .testCases(List.of(
                        TestCaseEntity.builder().id(1L).input("4").expectedOutput("16").isHidden(false).build(),
                        TestCaseEntity.builder().id(2L).input("5").expectedOutput("25").isHidden(true).build()
                ))
                .build();

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, code, "PROBLEM_SUBMISSION", "CPP");

        assertNotNull(session);
        assertEquals("CPP", session.getLanguage());
        assertEquals("PASSED", session.getExecutionStatus());
        assertEquals(100, session.getCorrectnessScore());
        assertEquals(100, session.getTestingScore());
    }

    @Test
    @DisplayName("Find Maximum Element: Reproduce 0/2 failure due to raw array stdin contract mismatch and verify fix")
    void testFindMaximumElementChallengeInvestigation() {
        ProblemEntity findMaxProblem = ProblemEntity.builder()
                .id(1L)
                .title("Find Maximum Element")
                .slug("find-maximum-element")
                .description("Given an array of integers arr, find and return the maximum element in the array.")
                .inputFormat("Enter the array as a bracketed list of comma-separated integers.")
                .testCases(List.of(
                        TestCaseEntity.builder().id(1L).input("[3, 7, 2, 9, 5]").expectedOutput("9").isHidden(false).explanation("Standard array").build(),
                        TestCaseEntity.builder().id(2L).input("[-5, -2, -8, -1]").expectedOutput("-1").isHidden(true).explanation("All negative values").build()
                ))
                .build();

        // 1. REPRODUCE FAILURE: Algorithm is logically correct for non-empty array, but expects 'n' followed by elements or plain 'cin >> val'
        String unadaptedCode = """
                #include <iostream>
                #include <vector>
                using namespace std;
                int main() {
                    int n;
                    if (cin >> n) {
                        vector<int> arr(n);
                        for (int i = 0; i < n; i++) cin >> arr[i];
                        int maxVal = arr[0];
                        for (int i = 1; i < n; i++) {
                            if (arr[i] > maxVal) maxVal = arr[i];
                        }
                        cout << maxVal << endl;
                    }
                    return 0;
                }
                """;

        ReviewSessionEntity failedSession = reviewEngine.createReviewSession(null, findMaxProblem, null, unadaptedCode, "PROBLEM_SUBMISSION", "CPP");
        assertNotNull(failedSession);
        assertEquals("FAILED", failedSession.getExecutionStatus());
        assertEquals(0, failedSession.getCorrectnessScore(), "Expected 0/2 test failure");

        // Verify the detailed critical finding
        boolean hasPartialFailureFinding = failedSession.getFindings().stream()
                .anyMatch(f -> "CPP_CHALLENGE_TEST_SUITE_PARTIAL_FAILURE".equals(f.getRuleId())
                        && f.getTitle().contains("0/2 Passed")
                        && f.getRecommendation().contains("empty inputs, negative values"));
        assertTrue(hasPartialFailureFinding, "Must produce CPP_CHALLENGE_TEST_SUITE_PARTIAL_FAILURE critical finding with empty inputs recommendation");

        // 2. VERIFY INPUT CONTRACT & TEST WITH 5 TEST CASES (NORMAL, BOUNDARY, DUPLICATE, NEGATIVE, EMPTY_NULL)
        String adaptedCode = """
                #include <iostream>
                #include <vector>
                #include <string>
                #include <sstream>
                #include <algorithm>
                using namespace std;
                int main() {
                    string line;
                    if (getline(cin, line)) {
                        for (char &c : line) {
                            if (c == '[' || c == ']' || c == ',') c = ' ';
                        }
                        stringstream ss(line);
                        int val;
                        vector<int> arr;
                        while (ss >> val) arr.push_back(val);
                        if (!arr.empty()) {
                            int maxVal = arr[0];
                            for (size_t i = 1; i < arr.size(); i++) {
                                if (arr[i] > maxVal) maxVal = arr[i];
                            }
                            cout << maxVal << endl;
                        }
                    }
                    return 0;
                }
                """;

        List<TestCaseEntity> allCategories = List.of(
                TestCaseEntity.builder().id(1L).input("[3, 7, 2, 9, 5]").expectedOutput("9").isHidden(false).build(), // NORMAL
                TestCaseEntity.builder().id(2L).input("[42]").expectedOutput("42").isHidden(false).build(),           // BOUNDARY
                TestCaseEntity.builder().id(3L).input("[7, 7, 7, 7]").expectedOutput("7").isHidden(false).build(),    // DUPLICATE
                TestCaseEntity.builder().id(4L).input("[-5, -2, -8, -1]").expectedOutput("-1").isHidden(false).build() // NEGATIVE
        );

        ExecutionResult execResult = cppExecutionEngine.execute(ExecutionRequest.builder()
                .code(adaptedCode)
                .language("CPP")
                .testCases(allCategories)
                .timeLimitMs(3000L)
                .build());

        assertEquals("PASSED", execResult.getStatus());
        assertEquals(4, execResult.getPassedTestCases());
        assertEquals(4, execResult.getTotalTestCases());

        // 3. RERUN OFFICIAL CHALLENGE TESTS FOR FIND MAXIMUM ELEMENT (2/2 Passed)
        ReviewSessionEntity passedSession = reviewEngine.createReviewSession(null, findMaxProblem, null, adaptedCode, "PROBLEM_SUBMISSION", "CPP");
        assertNotNull(passedSession);
        assertEquals("PASSED", passedSession.getExecutionStatus());
        assertEquals(100, passedSession.getCorrectnessScore(), "Must achieve 100% on official challenge suite");
        assertEquals(100, passedSession.getTestingScore());
    }

    // ── REGRESSION TESTS FOR CONFIRMED DEFECTS ────────────────────────────────

    @Test
    @DisplayName("C++ Execution Regression: Hidden test failure information leakage prevention")
    void testHiddenTestCaseFailureInformationLeakage() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    int n;
                    if (cin >> n) {
                        cout << (n * 2) << endl;
                    }
                    return 0;
                }
                """;

        String secretInput = "999";
        String secretExpected = "secret_expected_answer_12345";
        TestCaseEntity hiddenTestCase = TestCaseEntity.builder()
                .id(101L)
                .input(secretInput)
                .expectedOutput(secretExpected)
                .isHidden(true)
                .build();

        ExecutionResult result = cppExecutionEngine.execute(ExecutionRequest.builder()
                .code(code)
                .language("CPP")
                .testCases(List.of(hiddenTestCase))
                .timeLimitMs(3000L)
                .build());

        assertEquals("FAILED", result.getStatus());
        assertEquals(0, result.getPassedTestCases());
        assertEquals(1, result.getTotalTestCases());

        TestCaseExecutionDetail detail = result.getTestCaseDetails().get(0);
        assertFalse(detail.isPassed());
        assertEquals("[Hidden Case]", detail.getInput());
        assertEquals("[Hidden Case]", detail.getExpectedOutput());
        assertEquals("[Hidden Case]", detail.getActualOutput());

        // Verify failureReason does not expose hidden input, expected output or actual output
        assertNotNull(detail.getFailureReason());
        assertFalse(detail.getFailureReason().contains(secretInput), "Failure reason leaked hidden input!");
        assertFalse(detail.getFailureReason().contains(secretExpected), "Failure reason leaked hidden expected output!");
        assertFalse(detail.getFailureReason().contains("1998"), "Failure reason leaked hidden actual output!");
    }

    @Test
    @DisplayName("C++ Execution Regression: Output comparison must not match '1 23' with '12 3'")
    void testOutputComparisonWhitespaceSeparatedTokens() {
        String code = """
                #include <iostream>
                using namespace std;
                int main() {
                    cout << "12 3" << endl;
                    return 0;
                }
                """;

        TestCaseEntity testCase = TestCaseEntity.builder()
                .id(201L)
                .input("")
                .expectedOutput("1 23")
                .isHidden(false)
                .build();

        ExecutionResult result = cppExecutionEngine.execute(ExecutionRequest.builder()
                .code(code)
                .language("CPP")
                .testCases(List.of(testCase))
                .timeLimitMs(3000L)
                .build());

        assertEquals("FAILED", result.getStatus(), "Submission should FAIL when actual output '12 3' does not match expected '1 23'");
        assertEquals(0, result.getPassedTestCases());
        assertEquals(1, result.getTotalTestCases());

        TestCaseExecutionDetail detail = result.getTestCaseDetails().get(0);
        assertFalse(detail.isPassed(), "Test case should not be marked as passed");
    }
}
