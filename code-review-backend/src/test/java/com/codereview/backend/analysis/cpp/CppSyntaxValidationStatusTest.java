package com.codereview.backend.analysis.cpp;

import com.codereview.backend.ai.DeterministicAIReviewAdapter;
import com.codereview.backend.analysis.core.*;
import com.codereview.backend.analysis.java.JavaLanguageAnalyzer;
import com.codereview.backend.analysis.performance.PerformanceAnalyzer;
import com.codereview.backend.entity.ReviewSessionEntity;
import com.codereview.backend.execution.ExecutionEngine;
import com.codereview.backend.execution.ExecutionResult;
import com.codereview.backend.execution.cpp.CppCompilerService;
import com.codereview.backend.review.ReviewEngine;
import com.codereview.backend.service.StuckUserTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CppSyntaxValidationStatusTest {

    private CppCompilerService mockCompiler;
    private CppLanguageAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        mockCompiler = mock(CppCompilerService.class);
        analyzer = new CppLanguageAnalyzer(mockCompiler);
    }

    @Test
    @DisplayName("1. Valid C++ with compiler available reports SyntaxStatus.VALID")
    void testValidCppWithCompilerAvailable() {
        when(mockCompiler.isCompilerAvailable()).thenReturn(true);
        when(mockCompiler.checkSyntax(any()))
                .thenReturn(new CppCompilerService.CompilationResult(true, "", null, 15L));

        String code = """
                #include <iostream>
                int main() {
                    std::cout << "Hello, World!" << std::endl;
                    return 0;
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("CPP").build());

        assertNotNull(result);
        assertEquals(SyntaxStatus.VALID, result.getSyntaxStatus());
        assertTrue(result.isSyntaxValid());
        assertFalse(result.isSyntaxInvalid());
        assertFalse(result.isSyntaxUnavailable());
        assertEquals(Boolean.TRUE, result.getSyntaxValid());
        assertNull(result.getParsingErrorMessage());

        // Must not contain syntax error or unavailable findings
        assertFalse(result.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_ERROR".equals(f.getRuleId())));
        assertFalse(result.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_CHECK_UNAVAILABLE".equals(f.getRuleId())));
    }

    @Test
    @DisplayName("2. Invalid C++ with compiler available reports SyntaxStatus.INVALID and contains error finding")
    void testInvalidCppWithCompilerAvailable() {
        when(mockCompiler.isCompilerAvailable()).thenReturn(true);
        when(mockCompiler.checkSyntax(any()))
                .thenReturn(new CppCompilerService.CompilationResult(false, "error: expected ';' before '}' token", null, 12L));

        String code = """
                #include <iostream>
                int main() {
                    int x = 5
                    return 0;
                }
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("CPP").build());

        assertNotNull(result);
        assertEquals(SyntaxStatus.INVALID, result.getSyntaxStatus());
        assertFalse(result.isSyntaxValid());
        assertTrue(result.isSyntaxInvalid());
        assertFalse(result.isSyntaxUnavailable());
        assertEquals(Boolean.FALSE, result.getSyntaxValid());
        assertNotNull(result.getParsingErrorMessage());
        assertTrue(result.getParsingErrorMessage().contains("expected ';'"));

        // Must contain CPP_SYNTAX_ERROR finding
        assertTrue(result.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_ERROR".equals(f.getRuleId())));
    }

    @Test
    @DisplayName("3. Compiler unavailable with balanced but invalid-looking source reports SyntaxStatus.UNAVAILABLE, not VALID or INVALID")
    void testCompilerUnavailableWithBalancedInvalidLookingSource() {
        when(mockCompiler.isCompilerAvailable()).thenReturn(false);

        // Balanced delimiters, but complete nonsense that is NOT valid C++
        String code = "function foo bar { [ ( 123 + 456 ) ] } non valid c++ syntax ;;;";

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("CPP").build());

        assertNotNull(result);
        // CRUCIAL: Do not report VALID based solely on bracket balancing!
        assertNotEquals(SyntaxStatus.VALID, result.getSyntaxStatus(), "Must not claim VALID when compiler is unavailable");
        // CRUCIAL: Do not represent unavailable syntax verification as invalid syntax!
        assertNotEquals(SyntaxStatus.INVALID, result.getSyntaxStatus(), "Must not claim INVALID without compiler evidence");

        assertEquals(SyntaxStatus.UNAVAILABLE, result.getSyntaxStatus());
        assertFalse(result.isSyntaxValid(), "isSyntaxValid() must be false because syntax was not verified");
        assertFalse(result.isSyntaxInvalid(), "isSyntaxInvalid() must be false because code was not proven invalid");
        assertTrue(result.isSyntaxUnavailable());
        assertNull(result.getSyntaxValid(), "syntaxValid Boolean must be null (neither true nor false)");
        assertNull(result.getParsingErrorMessage(), "parsingErrorMessage must be null when syntax check is unavailable");

        // Informative finding added
        assertTrue(result.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_CHECK_UNAVAILABLE".equals(f.getRuleId())),
                "Must include CPP_SYNTAX_CHECK_UNAVAILABLE finding");

        // Heuristic analysis features must still run safely
        assertNotNull(result.getQualityScore());
        assertNotNull(result.getComplexityScore());
        assertEquals("UNAVAILABLE", result.getMetrics().get("SYNTAX_STATUS"));
        assertEquals(false, result.getMetrics().get("COMPILER_AVAILABLE"));
    }

    @Test
    @DisplayName("4. Compiler unavailable with unbalanced source reports SyntaxStatus.INVALID")
    void testCompilerUnavailableWithUnbalancedSource() {
        when(mockCompiler.isCompilerAvailable()).thenReturn(false);

        // Unbalanced brace
        String code = """
                #include <iostream>
                int main() {
                    std::cout << "Unclosed brace";
                    return 0;
                """;

        AnalysisResult result = analyzer.analyze(AnalysisRequest.builder().code(code).language("CPP").build());

        assertNotNull(result);
        assertEquals(SyntaxStatus.INVALID, result.getSyntaxStatus());
        assertFalse(result.isSyntaxValid());
        assertTrue(result.isSyntaxInvalid());
        assertEquals(Boolean.FALSE, result.getSyntaxValid());
        assertNotNull(result.getParsingErrorMessage());
        assertTrue(result.getParsingErrorMessage().contains("Unbalanced"));

        // Must contain CPP_SYNTAX_ERROR finding
        assertTrue(result.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_ERROR".equals(f.getRuleId())));
    }

    @Test
    @DisplayName("5. Analyzer unavailable in ReviewEngine reports UNAVAILABLE and does not represent code as invalid")
    void testAnalyzerUnavailableInReviewEngine() {
        JavaLanguageAnalyzer mockJavaAnalyzer = mock(JavaLanguageAnalyzer.class);
        ExecutionEngine mockExecEngine = mock(ExecutionEngine.class);

        // CppLanguageAnalyzer is null
        ReviewEngine reviewEngine = new ReviewEngine(
                mockJavaAnalyzer,
                null,
                mockExecEngine,
                new DeterministicAIReviewAdapter(),
                new StuckUserTracker(),
                new PerformanceAnalyzer()
        );

        String cppCode = "#include <iostream>\nint main(){return 0;}";

        ReviewSessionEntity session = reviewEngine.createReviewSession(
                null, null, null, cppCode, "FREEFORM_REVIEW", "CPP"
        );

        assertNotNull(session);
        assertEquals("UNAVAILABLE", session.getSyntaxStatus(), "Session syntaxStatus must be UNAVAILABLE");

        // Java analyzer must NEVER be called
        verifyNoInteractions(mockJavaAnalyzer);

        // Must contain CPP_ANALYZER_UNAVAILABLE finding
        assertTrue(session.getFindings().stream().anyMatch(f -> "CPP_ANALYZER_UNAVAILABLE".equals(f.getRuleId())));

        // Syntax error rule must NOT be present (unavailable is not invalid)
        assertFalse(session.getFindings().stream().anyMatch(f -> "CPP_SYNTAX_ERROR".equals(f.getRuleId())));
        assertFalse(session.getFindings().stream().anyMatch(f -> "JAVA_SYNTAX_ERROR".equals(f.getRuleId())));
    }
}
