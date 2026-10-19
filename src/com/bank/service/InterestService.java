package com.bank.service;

import com.bank.account.Account;
import com.bank.repository.AccountRepository;

import java.math.BigDecimal;

/**
 * <b>Single Responsibility:</b> runs the month-end interest payout.
 *
 * <p><b>Open/Closed + Liskov:</b> notice there is no {@code if (account
 * instanceof SavingsAccount)} here. Every account knows its own interest
 * policy, so this loop works for every account type - including ones that
 * have not been invented yet.
 */
public class InterestService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    public InterestService(AccountRepository accountRepository, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    public void applyMonthlyInterest() {
        for (Account account : accountRepository.findAll()) {
            BigDecimal interest = account.calculateMonthlyInterest();
            if (interest.signum() > 0) {
                transactionService.payInterest(account, interest);
            }
        }
    }
}
