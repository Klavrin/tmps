package com.bank.account;

import com.bank.interest.InterestPolicy;

/**
 * A term deposit: money goes in and earns interest, but cannot be withdrawn.
 *
 * <p>
 * This is the class that shows <b>Liskov Substitution</b> in action.
 * A common mistake is to make it extend a class with {@code withdraw()} and
 * then override it to throw {@code UnsupportedOperationException}. That breaks
 * every piece of code that trusted the parent class. Instead, this class
 * extends {@link Account} (which never promised withdrawals), so there is no
 * promise to break - the compiler simply will not let you call
 * {@code withdraw} on it.
 */
public class FixedDepositAccount extends Account {

  private final int termInMonths;

  public FixedDepositAccount(String accountNumber, String ownerName,
      InterestPolicy interestPolicy, int termInMonths) {
    super(accountNumber, ownerName, interestPolicy);
    if (termInMonths <= 0) {
      throw new IllegalArgumentException("Term must be at least one month");
    }
    this.termInMonths = termInMonths;
  }

  @Override
  public String getAccountType() {
    return "Fixed Deposit (" + termInMonths + " months)";
  }

  public int getTermInMonths() {
    return termInMonths;
  }
}
