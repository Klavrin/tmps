package com.bank;

import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountNumberGenerator;
import com.bank.service.InterestService;
import com.bank.service.TransactionService;

/**
 * Builds the bank from its small parts and hands them out. Each part has
 * ONE job (SRP); this record only wires them together, so the interactive
 * app and the guided demo start from the same setup.
 */
public record Bank(
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        AccountNumberGenerator numberGenerator,
        TransactionService transactionService,
        InterestService interestService) {

    public static Bank create() {
        AccountRepository accountRepository = new AccountRepository();
        TransactionRepository transactionRepository = new TransactionRepository();
        TransactionService transactionService = new TransactionService(transactionRepository);
        return new Bank(
                accountRepository,
                transactionRepository,
                new AccountNumberGenerator(),
                transactionService,
                new InterestService(accountRepository, transactionService));
    }
}
