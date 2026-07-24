package com.albertchow.lifecompass.support.dto;

/** Response to an AskSupportRequest, returning the best matching FAQ answer. */
public record SupportAnswerResponse(
        /** The FAQ answer text, an AI-generated answer, or a generic fallback message. */
        String answer,
        /** True if a stored FAQ actually matched the question; false for both the AI and fallback paths. */
        boolean matched,
        /** Which path produced the answer: "faq", "ai", or "fallback". */
        String source) {
}
