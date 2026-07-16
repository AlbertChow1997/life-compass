package com.albertchow.lifecompass.security;

/**
 * Request-scoped holder for the current {@link LoginUser}, populated by
 * {@link JwtAuthenticationFilter} and cleared at the end of every request.
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** @throws IllegalStateException if called outside an authenticated request. */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("No authenticated user in the current request context");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
