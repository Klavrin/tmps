package com.bank.exception;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal (or the outgoing side of a transfer)
 * asks for more money than the account is allowed to give.
 */
public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String accountNumber, BigDecimal requested, BigDecimal available) {
    super("Account " + accountNumber + ": cannot withdraw " + requested
        + ", only " + available + " is available");
  }
}
