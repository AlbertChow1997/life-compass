package com.albertchow.lifecompass.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Answers a support question using DeepSeek's chat-completions API (an
 * OpenAI-compatible REST endpoint) when the FAQ keyword list has no match.
 * Mirrors {@link com.albertchow.lifecompass.security.TwilioSmsSender}'s
 * "degrade gracefully when not configured" pattern: {@link #isConfigured()}
 * lets {@link SupportService} skip straight to the fallback message when no
 * API key is set, instead of every call failing.
 */
@Slf4j
@Component
public class DeepSeekClient {

    private static final String SYSTEM_PROMPT = """
            You are the customer support assistant for LifeCompass, a local dining and
            entertainment directory and review app for Ireland. Users can browse and rate
            shops, write posts, follow other users, and buy vouchers. Answer the visitor's
            question about using the platform in 2-3 short sentences. If the question isn't
            about LifeCompass, politely say you can only help with LifeCompass-related
            questions.
            """;

    private final RestClient restClient;
    private final boolean configured;

    public DeepSeekClient(@Value("${lifecompass.deepseek.api-key:}") String apiKey) {
        this.configured = !apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /** True if a DeepSeek API key was supplied, i.e. {@link #ask} can actually be called. */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Asks DeepSeek the given question and returns its answer.
     *
     * @throws IllegalStateException if the API call fails or returns an unexpected shape;
     *     callers should catch this and fall back to a canned message rather than surface it.
     */
    public String ask(String question) {
        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", "deepseek-v4-flash",
                            "messages", List.of(
                                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                                    Map.of("role", "user", "content", question)),
                            "stream", false))
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IllegalStateException("DeepSeek returned no choices");
            }
            return response.choices().get(0).message().content().trim();
        } catch (RuntimeException e) {
            log.warn("DeepSeek call failed, caller should fall back", e);
            throw new IllegalStateException("DeepSeek call failed", e);
        }
    }

    /** Minimal shape of DeepSeek's (OpenAI-compatible) chat-completions response — only the fields we read. */
    private record ChatResponse(List<Choice> choices) {
        private record Choice(Message message) {
            private record Message(String content) {
            }
        }
    }
}
