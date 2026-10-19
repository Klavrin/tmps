package com.bank.service;

/**
 * <b>Single Responsibility:</b> hands out unique account numbers
 * such as ACC-1001, ACC-1002, ...
 */
public class AccountNumberGenerator {

    private int nextNumber = 1001;

    public String next() {
        return "ACC-" + nextNumber++;
    }
}
