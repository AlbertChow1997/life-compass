package com.albertchow.lifecompass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the LifeCompass backend — a shop/dining directory app for
 * Ireland with blogging, ratings, vouchers, and support features. Boots the
 * whole Spring application context.
 */
@SpringBootApplication
public class LifeCompassApplication {

    /** Starts the embedded server and Spring context. */
    public static void main(String[] args) {
        SpringApplication.run(LifeCompassApplication.class, args);
    }

}
