package com.bank.account;

import com.bank.exception.InsufficientFundsException;
import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;

public abstract class WithdrawableAccount extends Account {

  protected WithdrawableAccount(String accountNumber, String ownerName, InterestPolicy interestPolicy) {
    super(accountNumber, ownerName, interestPolicy);
  }

  /** The maximum amount that can be withdrawn right now. Never negative. */
  public abstract BigDecimal getAvailableFunds();

  /** Lets callers check before trying, instead of relying on exceptions. */
  public final boolean canWithdraw(BigDecimal amount) {
    return amount != null
        && amount.signum() > 0
        && amount.compareTo(getAvailableFunds()) <= 0;
  }

  public final void withdraw(BigDecimal amount) {
    requirePositive(amount);
    if (!canWithdraw(amount)) {
      throw new InsufficientFundsException(getAccountNumber(), amount, getAvailableFunds());
    }
    reduceBalance(amount);
  }
}
