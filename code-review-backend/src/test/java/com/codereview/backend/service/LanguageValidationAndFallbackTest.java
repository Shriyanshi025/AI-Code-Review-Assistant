package com.codereview.backend.service;

import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.analysis.core.*;
import com.codereview.backend.analysis.cpp.CppLanguageAnalyzer;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.analysis.performance.PerformanceAnalyzer;
import com.codereview.backend.controller.GlobalExceptionHandler;
import com.codereview.backend.controller.ReviewController;
import com.codereview.backend.controller.SubmissionController;
import com.codereview.backend.dto.FreeformReviewRequestDto;
import com.codereview.backend.dto.ProblemReviewRequestDto;
import com.codereview.backend.dto.SubmissionRequestDto;
import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.entity.SubmissionEntity;
import com.codereview.backend.entity.TestCaseEntity;
import com.codereview.backend.entity.UserEntity;
import com.codereview.backend.exception.InvalidLanguageException;
import com.codereview.backend.execution.*;
import com.codereview.backend.execution.cpp.CppExecutionEngine;
import com.codereview.backend.repository.ProblemRepository;
import com.codereview.backend.repository.ReviewSessionRepository;
import com.codereview.backend.repository.SubmissionRepository;
import com.codereview.backend.repository.UserRepository;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.util.LanguageValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LanguageValidationAndFallbackTest {

    private MockMvc submissionMockMvc;
    private MockMvc reviewMockMvc;
    private SubmissionService mockSubmissionService;
    private ReviewService mockReviewService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSubmissionService = mock(SubmissionService.class);
        mockReviewService = mock(ReviewService.class);

        SubmissionController submissionController = new SubmissionController(mockSubmissionService);
        submissionMockMvc = MockMvcBuilders.standaloneSetup(submissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        ReviewController reviewController = new ReviewController(mockReviewService);
        reviewMockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // 1. HTTP 400 on Unsupported or Blank Languages
    // =========================================================================

    @Test
    @DisplayName("1a. Unsupported language 'PYTHON' in submission returns HTTP 400 with clear message")
    void testUnsupportedLanguageReturnsHttp400OnSubmission() throws Exception {
        SubmissionRequestDto dto = new SubmissionRequestDto();
        dto.setProblemId(1L);
        dto.setCode("print('hello')");
        dto.setLanguage("PYTHON");

        submissionMockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported language: 'PYTHON'")));

        verifyNoInteractions(mockSubmissionService);
    }

    @Test
    @DisplayName("1b. Blank language in submission returns HTTP 400")
    void testBlankLanguageReturnsHttp400OnSubmission() throws Exception {
        SubmissionRequestDto dto = new SubmissionRequestDto();
        dto.setProblemId(1L);
        dto.setCode("class Solution {}");
        dto.setLanguage("   ");

        submissionMockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot be blank")));

        verifyNoInteractions(mockSubmissionService);
    }

    @Test
    @DisplayName("1c. Null language in submission returns HTTP 400")
    void testNullLanguageReturnsHttp400OnSubmission() throws Exception {
        SubmissionRequestDto dto = new SubmissionRequestDto();
        dto.setProblemId(1L);
        dto.setCode("class Solution {}");
        dto.setLanguage(null);

        submissionMockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("required and cannot be blank")));

        verifyNoInteractions(mockSubmissionService);
    }

    @Test
    @DisplayName("1d. Unsupported language 'GO' in freeform review returns HTTP 400")
    void testUnsupportedLanguageReturnsHttp400OnFreeformReview() throws Exception {
        FreeformReviewRequestDto dto = new FreeformReviewRequestDto();
        dto.setCode("package main\nfunc main() {}");
        dto.setLanguage("GO");

        reviewMockMvc.perform(post("/api/reviews/freeform")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported language: 'GO'")));

        verifyNoInteractions(mockReviewService);
    }

    @Test
    @DisplayName("1e. Unsupported language 'RUST' in problem review returns HTTP 400")
    void testUnsupportedLanguageReturnsHttp400OnProblemReview() throws Exception {
        ProblemReviewRequestDto dto = new ProblemReviewRequestDto();
        dto.setProblemId(1L);
        dto.setCode("fn main() {}");
        dto.setLanguage("RUST");

        reviewMockMvc.perform(post("/api/reviews/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported language: 'RUST'")));

        verifyNoInteractions(mockReviewService);
    }

    // =========================================================================
    // 2. Language Normalization and Alias Handling
    // =========================================================================

    @Test
    @DisplayName("2a. Valid Java aliases normalize to 'JAVA'")
    void testValidJavaAliases() {
        assertEquals("JAVA", LanguageValidator.validateAndNormalize("JAVA"));
        assertEquals("JAVA", LanguageValidator.validateAndNormalize("java"));
        assertEquals("JAVA", LanguageValidator.validateAndNormalize("Java"));
        assertEquals("JAVA", LanguageValidator.validateAndNormalize("  JAVA  "));
        assertEquals("JAVA", LanguageValidator.validateAndNormalize("  java\t"));
        assertTrue(LanguageValidator.isJava("java"));
        assertFalse(LanguageValidator.isJava("cpp"));
    }

    @Test
    @DisplayName("2b. Valid C++ aliases normalize to 'CPP'")
    void testValidCppAliases() {
        assertEquals("CPP", LanguageValidator.validateAndNormalize("CPP"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("cpp"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("Cpp"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("C++"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("c++"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("  C++  "));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("  cpp\n"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("CPLUSPLUS"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("cplusplus"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("CXX"));
        assertEquals("CPP", LanguageValidator.validateAndNormalize("cxx"));
        assertTrue(LanguageValidator.isCpp("C++"));
        assertTrue(LanguageValidator.isCpp("cxx"));
        assertFalse(LanguageValidator.isCpp("java"));
    }

    @Test
    @DisplayName("2c. Controller normalizes 'c++' to 'CPP' and succeeds")
    void testControllerNormalizesAliases() throws Exception {
        SubmissionRequestDto dto = new SubmissionRequestDto();
        dto.setProblemId(1L);
        dto.setCode("#include <iostream>\nint main(){return 0;}");
        dto.setLanguage("c++");

        SubmissionEntity dummySubmission = com.codereview.backend.entity.SubmissionEntity.builder().id(1L).status("PASSED").language("CPP").build();
        ExecutionResult dummyExec = ExecutionResult.builder().status("PASSED").passedTestCases(1).totalTestCases(1).build();
        ReviewSessionEntity dummyReview = ReviewSessionEntity.builder().id(1L).language("CPP").overallScore(95).build();
        SubmissionService.SubmissionResponse dummyResponse = new SubmissionService.SubmissionResponse(dummySubmission, dummyReview, dummyExec, List.of());

        when(mockSubmissionService.submitSolution(eq(1L), any(), any(), eq("CPP")))
                .thenReturn(dummyResponse);

        submissionMockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(mockSubmissionService, times(1)).submitSolution(eq(1L), any(), any(), eq("CPP"));
    }

    // =========================================================================
    // 3. Missing C++ Execution Engine Does NOT Invoke Java Execution
    // =========================================================================

    @Test
    @DisplayName("3a. ValidationExecutionEngine with missing CppExecutionEngine returns explicit error and never invokes Java")
    void testMissingCppExecutionEngineDoesNotInvokeJava() {
        JavaLanguageAnalyzer mockJavaAnalyzer = mock(JavaLanguageAnalyzer.class);
        // CppExecutionEngine is null
        ValidationExecutionEngine engine = new ValidationExecutionEngine(mockJavaAnalyzer, null);

        String cppCode = """
                #include <iostream>
                int main() {
                    std::cout << "Hello" << std::endl;
                    return 0;
                }
                """;

        ExecutionRequest request = ExecutionRequest.builder()
                .code(cppCode)
                .language("CPP")
                .testCases(List.of(TestCaseEntity.builder().id(1L).input("").expectedOutput("Hello").build()))
                .build();

        ExecutionResult result = engine.execute(request);

        assertNotNull(result);
        assertEquals("NOT_APPLICABLE", result.getStatus());
        assertFalse(result.isApplicable());
        assertTrue(result.getApplicabilityReason().contains("C++ execution engine is unavailable"));
        assertTrue(result.getUserFacingSummary().contains("C++ execution engine is not configured"));

        // Crucial: JavaLanguageAnalyzer must NEVER be invoked for C++ execution!
        verifyNoInteractions(mockJavaAnalyzer);
    }

    @Test
    @DisplayName("3b. Freeform execution with missing CppExecutionEngine returns explicit error and never invokes Java")
    void testMissingCppExecutionEngineFreeformDoesNotInvokeJava() {
        JavaLanguageAnalyzer mockJavaAnalyzer = mock(JavaLanguageAnalyzer.class);
        ValidationExecutionEngine engine = new ValidationExecutionEngine(mockJavaAnalyzer, null);

        String cppCode = "#include <iostream>\nint main(){return 0;}";
        ExecutionResult result = engine.executeFreeform(cppCode, 2000L, "CPP");

        assertNotNull(result);
        assertEquals("NOT_APPLICABLE", result.getStatus());
        assertFalse(result.isApplicable());
        assertTrue(result.getApplicabilityReason().contains("C++ execution engine is unavailable"));

        verifyNoInteractions(mockJavaAnalyzer);
    }

    // =========================================================================
    // 4. Missing C++ Analyzer Does NOT Invoke Java Analysis
    // =========================================================================

    @Test
    @DisplayName("4. ReviewEngine with missing CppLanguageAnalyzer returns explicit analyzer finding and never invokes Java analyzer")
    void testMissingCppAnalyzerDoesNotInvokeJava() {
        JavaLanguageAnalyzer mockJavaAnalyzer = mock(JavaLanguageAnalyzer.class);
        ExecutionEngine mockExecEngine = mock(ExecutionEngine.class);
        when(mockExecEngine.execute(any())).thenReturn(ExecutionResult.builder().status("PASSED").passedTestCases(1).totalTestCases(1).build());

        // CppLanguageAnalyzer is null
        ReviewEngine reviewEngine = new ReviewEngine(
                mockJavaAnalyzer,
                null,
                mockExecEngine,
                new DeterministicAIReviewAdapter(),
                new StuckUserTracker(),
                new PerformanceAnalyzer()
        );

        String cppCode = """
                #include <vector>
                using namespace std;
                int main() {
                    return 0;
                }
                """;

        ProblemEntity problem = ProblemEntity.builder()
                .id(1L)
                .title("Two Sum")
                .description("Two Sum description")
                .testCases(List.of(TestCaseEntity.builder().id(1L).input("[2, 7]").expectedOutput("[0, 1]").build()))
                .build();

        ReviewSessionEntity session = reviewEngine.createReviewSession(null, problem, null, cppCode, "PROBLEM_SUBMISSION", "CPP");

        assertNotNull(session);
        // JavaLanguageAnalyzer must NEVER be invoked for C++ source code!
        verifyNoInteractions(mockJavaAnalyzer);

        // Confirm explicit finding that C++ analyzer is unavailable was added
        boolean hasUnavailableFinding = session.getFindings().stream()
                .anyMatch(f -> "CPP_ANALYZER_UNAVAILABLE".equals(f.getRuleId()));
        assertTrue(hasUnavailableFinding, "Must contain explicit CPP_ANALYZER_UNAVAILABLE finding");
    }

    // =========================================================================
    // 5. Existing Java and C++ Paths Still Work
    // =========================================================================

    @Test
    @DisplayName("5a. Existing Java submission path works cleanly")
    void testExistingJavaPathWorks() {
        JavaLanguageAnalyzer javaAnalyzer = new JavaLanguageAnalyzer();
        ValidationExecutionEngine engine = new ValidationExecutionEngine(javaAnalyzer);

        String validJava = """
                public class Solution {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;

        TestCaseEntity tc = TestCaseEntity.builder().id(1L).input("2, 3").expectedOutput("5").isHidden(false).build();
        ExecutionResult result = engine.execute(ExecutionRequest.builder()
                .code(validJava)
                .language("JAVA")
                .testCases(List.of(tc))
                .build());

        assertEquals("PASSED", result.getStatus());
        assertEquals(1, result.getPassedTestCases());
    }

    @Test
    @DisplayName("5b. C++ execution engine is invoked when configured")
    void testCppPathInvokedWhenAvailable() {
        JavaLanguageAnalyzer mockJavaAnalyzer = mock(JavaLanguageAnalyzer.class);
        CppExecutionEngine mockCppEngine = mock(CppExecutionEngine.class);

        when(mockCppEngine.execute(any())).thenReturn(ExecutionResult.builder()
                .status("PASSED")
                .passedTestCases(1)
                .totalTestCases(1)
                .build());

        ValidationExecutionEngine engine = new ValidationExecutionEngine(mockJavaAnalyzer, mockCppEngine);

        ExecutionRequest request = ExecutionRequest.builder()
                .code("#include <iostream>\nint main(){return 0;}")
                .language("CPP")
                .testCases(List.of())
                .build();

        ExecutionResult result = engine.execute(request);

        assertEquals("PASSED", result.getStatus());
        verify(mockCppEngine, times(1)).execute(request);
        verifyNoInteractions(mockJavaAnalyzer);
    }
}
