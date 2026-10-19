package com.bank.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The same yearly rate for every balance.
 * Example: 3% per year on 1,200.00 earns 1,200.00 * 3 / 100 / 12 = 3.00 per month.
 */
public class FixedRateInterest implements InterestPolicy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private final BigDecimal annualRatePercent;

    public FixedRateInterest(double annualRatePercent) {
        if (annualRatePercent < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        this.annualRatePercent = BigDecimal.valueOf(annualRatePercent);
    }

    @Override
    public BigDecimal monthlyInterestFor(BigDecimal balance) {
        if (balance.signum() <= 0) {
            return BigDecimal.ZERO; // no interest on an empty or overdrawn balance
        }
        return balance
                .multiply(annualRatePercent)
                .divide(ONE_HUNDRED.multiply(MONTHS_PER_YEAR), 2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String describe() {
        return annualRatePercent.stripTrailingZeros().toPlainString() + "% per year";
    }
}
