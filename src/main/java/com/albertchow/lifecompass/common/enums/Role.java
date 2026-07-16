package com.albertchow.lifecompass.common.enums;

/**
 * Account role. Stored as a string in {@code user.role}.
 */
public enum Role {
    /** Regular end user (Google or SMS login). */
    USER,
    /** Business owner who manages their shop's vouchers. */
    MERCHANT,
    /** Site administrator who moderates posts and comments. */
    ADMIN;

    /** Spring Security authority name, e.g. {@code ROLE_ADMIN}. */
    public String authority() {
        return "ROLE_" + name();
    }
}
