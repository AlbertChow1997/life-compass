package com.albertchow.lifecompass.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Wires up the Google ID token verifier used by {@link AuthController}'s
 * Google sign-in endpoint to check that a token was really issued by Google
 * for this app's client ID.
 */
@Configuration
public class GoogleAuthConfig {

    /** Builds the verifier that checks a Google ID token's signature and confirms it was issued for our app's client ID. */
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(
            @Value("${lifecompass.google.client-id}") String clientId) {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
}
