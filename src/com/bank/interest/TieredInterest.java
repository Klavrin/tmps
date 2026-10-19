package com.bank.interest;

import java.math.BigDecimal;

/**
 * A higher rate for bigger balances: below the threshold the lower rate is
 * used, at or above it the higher rate is used.
 *
 * <p>This class was added WITHOUT touching any existing code - it simply
 * reuses {@link FixedRateInterest}. That is the Open/Closed principle.
 */
public class TieredInterest implements InterestPolicy {

    private final BigDecimal threshold;
    private final FixedRateInterest lowerTier;
    private final FixedRateInterest upperTier;

    public TieredInterest(BigDecimal threshold, double lowerRatePercent, double upperRatePercent) {
        this.threshold = threshold;
        this.lowerTier = new FixedRateInterest(lowerRatePercent);
        this.upperTier = new FixedRateInterest(upperRatePercent);
    }

    @Override
    public BigDecimal monthlyInterestFor(BigDecimal balance) {
        InterestPolicy tier = balance.compareTo(threshold) >= 0 ? upperTier : lowerTier;
        return tier.monthlyInterestFor(balance);
    }

    @Override
    public String describe() {
        return lowerTier.describe() + " below " + threshold.toPlainString()
                + ", " + upperTier.describe() + " above";
    }
}
