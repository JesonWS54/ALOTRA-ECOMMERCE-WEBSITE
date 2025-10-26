package com.alotra.entity.enums;

/**
 * Payment Method Enum
 * Represents available payment options
 */
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    CREDIT_CARD("Thẻ tín dụng"),
    MOMO("Ví MoMo"),
    ZALOPAY("Ví ZaloPay"),
    VNPAY("VNPAY");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String method) {
        if (method == null) {
            return COD;
        }
        
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COD;
        }
    }
}