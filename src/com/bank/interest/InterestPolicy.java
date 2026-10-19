package com.bank.interest;

import java.math.BigDecimal;

/**
 * A rule for calculating how much interest a balance earns in one month.
 *
 * <p><b>Open/Closed:</b> the bank is OPEN for new interest rules (just write a
 * new class that implements this interface) but CLOSED for modification
 * (Account, InterestService and the other policies never need to change).
 * Compare that with a big {@code switch (accountType)} block, which you would
 * have to edit every time a new rule appears.
 */
public interface InterestPolicy {

    /** Returns the interest earned this month, rounded to cents. Never negative. */
    BigDecimal monthlyInterestFor(BigDecimal balance);

    /** A short description, used when printing statements. */
    String describe();
}
