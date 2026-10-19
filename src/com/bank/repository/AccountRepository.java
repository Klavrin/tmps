package com.bank.repository;

import com.bank.account.Account;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Single Responsibility:</b> stores accounts and finds them again.
 * Nothing else. It keeps them in memory; switching to a database
 * would only change this class.
 */
public class AccountRepository {

    private final Map<String, Account> accountsByNumber = new LinkedHashMap<>();

    public void save(Account account) {
        accountsByNumber.put(account.getAccountNumber(), account);
    }

    public Optional<Account> findByNumber(String accountNumber) {
        return Optional.ofNullable(accountsByNumber.get(accountNumber));
    }

    public List<Account> findAll() {
        return new ArrayList<>(accountsByNumber.values());
    }
}
