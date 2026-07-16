package com.albertchow.lifecompass.common.enums;

/**
 * Lifecycle status stored in {@code voucher_order.status}.
 */
public enum OrderStatus {
    UNPAID(1),
    PAID(2),
    USED(3),
    CANCELLED(4),
    REFUNDED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
