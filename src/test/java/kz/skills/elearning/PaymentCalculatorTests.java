package kz.skills.elearning;

import java.math.BigDecimal;
import kz.skills.elearning.lab.PaymentCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentCalculatorTests {
    @Test
    void totalIncludesQuantity() {
        assertEquals(
                new BigDecimal("3000"),
                PaymentCalculator.calculateTotal(new BigDecimal("1000"), 3)
        );
    }
}