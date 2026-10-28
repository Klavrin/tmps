package com.bank.account;

import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;

/**
 * An everyday account with an overdraft: the balance may go below zero,
 * but never further than {@code -overdraftLimit}.
 */
public class CheckingAccount extends WithdrawableAccount {

    private final BigDecimal overdraftLimit;

    public CheckingAccount(String accountNumber, String ownerName,
                           InterestPolicy interestPolicy, BigDecimal overdraftLimit) {
        super(accountNumber, ownerName, interestPolicy);
        if (overdraftLimit.signum() < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative");
        }
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public BigDecimal getAvailableFunds() {
        BigDecimal available = getBalance().add(overdraftLimit);
        return available.max(BigDecimal.ZERO);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    @Override
    public Account copy(String newAccountNumber, String newOwnerName) {
        return new CheckingAccount(newAccountNumber, newOwnerName, getInterestPolicy(), overdraftLimit);
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}
