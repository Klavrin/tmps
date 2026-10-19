package com.bank.repository;

import com.bank.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Single Responsibility:</b> stores the transaction history.
 * Accounts do not keep their own history - that would give them two jobs.
 */
public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.accountNumber().equals(accountNumber))
                .toList();
    }
}
