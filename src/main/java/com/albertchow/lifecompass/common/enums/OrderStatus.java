package com.albertchow.lifecompass.common.enums;

/**
 * Lifecycle status stored in {@code voucher_order.status}.
 */
public enum OrderStatus {
    /** Order created but payment not yet completed. */
    UNPAID(1),
    /** Payment completed; voucher not yet redeemed. */
    PAID(2),
    /** Voucher has been redeemed at the shop. */
    USED(3),
    /** Order cancelled before payment. */
    CANCELLED(4),
    /** Payment was refunded after the fact. */
    REFUNDED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
