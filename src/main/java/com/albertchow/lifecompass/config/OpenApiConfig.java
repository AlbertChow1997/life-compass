package com.albertchow.lifecompass.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes the OpenAPI/Swagger UI description of every REST endpoint at
 * /swagger-ui/index.html, with a "bearerAuth" scheme so protected routes can
 * be exercised directly from the browser: log in through one of the
 * /api/auth/... endpoints, copy the returned token, and paste it into the
 * Authorize dialog (just the raw token — Swagger UI adds the "Bearer " prefix).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LifeCompass API",
                version = "v1",
                description = "Shop directory, ratings, posts, vouchers, and personal-centre APIs for the LifeCompass project."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
