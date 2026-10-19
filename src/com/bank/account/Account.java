package com.bank.account;

import com.bank.exception.InvalidAmountException;
import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class Account {

  private final String accountNumber;
  private final String ownerName;
  private final InterestPolicy interestPolicy;
  private BigDecimal balance = BigDecimal.ZERO;

  protected Account(String accountNumber, String ownerName, InterestPolicy interestPolicy) {
    this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
    this.ownerName = Objects.requireNonNull(ownerName, "ownerName");
    this.interestPolicy = Objects.requireNonNull(interestPolicy, "interestPolicy");
  }

  public final void deposit(BigDecimal amount) {
    requirePositive(amount);
    balance = balance.add(amount);
  }

  public final BigDecimal calculateMonthlyInterest() {
    return interestPolicy.monthlyInterestFor(balance);
  }

  public abstract String getAccountType();

  protected final void reduceBalance(BigDecimal amount) {
    balance = balance.subtract(amount);
  }

  protected static void requirePositive(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new InvalidAmountException(amount);
    }
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public InterestPolicy getInterestPolicy() {
    return interestPolicy;
  }

  @Override
  public String toString() {
    return getAccountType() + " " + accountNumber + " (" + ownerName + ")";
  }
}
