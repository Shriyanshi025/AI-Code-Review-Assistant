package com.codereview.backend.analysis.core;

public interface LanguageAnalyzer {

    /**
     * Returns the target language identifier (e.g., "JAVA").
     */
    String getSupportedLanguage();

    /**
     * Performs structural AST parsing and deterministic static code analysis.
     *
     * @param request the analysis request containing source code and context
     * @return language-independent analysis result
     */
    AnalysisResult analyze(AnalysisRequest request);
}
