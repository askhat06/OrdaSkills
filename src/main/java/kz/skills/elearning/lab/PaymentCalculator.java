package kz.skills.elearning.lab;

import java.math.BigDecimal;

public final class PaymentCalculator {

    private PaymentCalculator() {
    }

    public static BigDecimal calculateTotal(
            BigDecimal unitPrice, int quantity) {
        if (unitPrice == null || unitPrice.signum() < 0 || quantity < 1) {
            throw new IllegalArgumentException("Invalid price or quantity");
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));// Учебная ошибка: количество товаров не учитывается.

    }
}