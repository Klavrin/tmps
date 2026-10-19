package com.bank.account;

import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;

/**
 * A savings account must always keep a minimum balance.
 * Only the money above that minimum can be withdrawn.
 */
public class SavingsAccount extends WithdrawableAccount {

    private final BigDecimal minimumBalance;

    public SavingsAccount(String accountNumber, String ownerName,
                          InterestPolicy interestPolicy, BigDecimal minimumBalance) {
        super(accountNumber, ownerName, interestPolicy);
        if (minimumBalance.signum() < 0) {
            throw new IllegalArgumentException("Minimum balance cannot be negative");
        }
        this.minimumBalance = minimumBalance;
    }

    @Override
    public BigDecimal getAvailableFunds() {
        BigDecimal available = getBalance().subtract(minimumBalance);
        return available.max(BigDecimal.ZERO);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }
}
