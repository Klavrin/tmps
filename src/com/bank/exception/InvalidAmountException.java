package com.bank.exception;

import java.math.BigDecimal;

/**
 * Thrown when someone tries to deposit, withdraw or transfer
 * an amount that is null, zero or negative.
 */
public class InvalidAmountException extends IllegalArgumentException {

    public InvalidAmountException(BigDecimal amount) {
        super("Amount must be greater than zero, but was: " + amount);
    }
}
