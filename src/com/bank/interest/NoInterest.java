package com.bank.interest;

import java.math.BigDecimal;

/** For accounts that do not earn interest, such as most checking accounts. */
public class NoInterest implements InterestPolicy {

    @Override
    public BigDecimal monthlyInterestFor(BigDecimal balance) {
        return BigDecimal.ZERO;
    }

    @Override
    public String describe() {
        return "no interest";
    }
}
