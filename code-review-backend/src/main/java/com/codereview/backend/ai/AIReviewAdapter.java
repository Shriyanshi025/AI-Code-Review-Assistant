package com.codereview.backend.ai;

public interface AIReviewAdapter {

    /**
     * Generates a structured AI review based on code, AST analysis metrics, deterministic findings, and problem context.
     *
     * @param request the structured AI review request
     * @return structured AI review response
     */
    StructuredAIResponse generateReview(AIReviewRequest request);
}
