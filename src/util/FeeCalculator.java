package util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fee rules of Wallet++.
 * The assignment says "fees (if any)" without fixing a rate, so this project
 * uses a simple rule: transfers up to Rs. 1,000 are free; above that a 1% fee
 * is charged. Top-ups, payments and refunds have no fee.
 */
public final class FeeCalculator {

    public static final BigDecimal FREE_TRANSFER_LIMIT = new BigDecimal("1000.00");
    public static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");

    private FeeCalculator() {
        // utility class - no objects needed
    }

    public static BigDecimal transferFee(BigDecimal amount) {
        if (amount.compareTo(FREE_TRANSFER_LIMIT) <= 0) {
            return new BigDecimal("0.00");
        }
        return amount.multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
