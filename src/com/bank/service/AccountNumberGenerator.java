package com.bank.service;

import com.bank.creational.singleton.BankConfig;

/**
 * <b>Single Responsibility:</b> hands out unique account numbers
 * such as ACC-1001, ACC-1002, ...
 *
 * <p>The prefix and the first number come from the {@link BankConfig}
 * singleton, so the format is configured in exactly one place.
 */
public class AccountNumberGenerator {

    private final String prefix;
    private int nextNumber;

    public AccountNumberGenerator() {
        BankConfig config = BankConfig.getInstance();
        this.prefix = config.accountNumberPrefix();
        this.nextNumber = config.firstAccountNumber();
    }

    public String next() {
        return prefix + nextNumber++;
    }
}
