package com.albertchow.lifecompass.common.enums;

/**
 * Lifecycle status stored in {@code voucher.status}.
 * Merchants toggle between {@link #ON_SHELF} and {@link #OFF_SHELF}.
 */
public enum VoucherStatus {
    /** Visible and purchasable by customers. */
    ON_SHELF(1),
    /** Hidden by the merchant; not purchasable. */
    OFF_SHELF(2),
    /** Past its end time; not purchasable. */
    EXPIRED(3);

    private final int code;

    VoucherStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
