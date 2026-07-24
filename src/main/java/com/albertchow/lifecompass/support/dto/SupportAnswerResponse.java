package com.albertchow.lifecompass.support.dto;

/** Response to an AskSupportRequest, returning the best matching FAQ answer. */
public record SupportAnswerResponse(
        /** The FAQ answer text, or a generic fallback message if nothing matched. */
        String answer,
        /** True if a stored FAQ actually matched the question; false if this is the fallback. */
        boolean matched) {
}
