package com.bank.transaction;

/** Every kind of money movement the bank records. */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
    INTEREST
}
