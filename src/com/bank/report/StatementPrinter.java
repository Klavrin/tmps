package com.bank.report;

import com.bank.account.Account;
import com.bank.console.Ansi;
import com.bank.repository.TransactionRepository;
import com.bank.transaction.Transaction;

import java.util.List;

/**
 * <b>Single Responsibility:</b> turns accounts and transactions into text.
 * If the bank wants HTML or PDF statements, only this kind of class changes -
 * the accounts and services are not affected.
 */
public class StatementPrinter {

    private static final String LINE = Ansi.dim("-".repeat(72));

    private final TransactionRepository transactionRepository;

    public StatementPrinter(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void printStatement(Account account) {
        System.out.println(LINE);
        System.out.println(Ansi.style("1;36", "Statement for " + account));
        System.out.println(Ansi.dim("Interest: ") + account.getInterestPolicy().describe());
        System.out.println(LINE);
        System.out.println(Ansi.bold(String.format("%-13s %10s %12s   %s", "TYPE", "AMOUNT", "BALANCE", "DESCRIPTION")));

        List<Transaction> transactions = transactionRepository.findByAccountNumber(account.getAccountNumber());
        if (transactions.isEmpty()) {
            System.out.println(Ansi.dim("(no transactions)"));
        }
        for (Transaction t : transactions) {
            System.out.printf("%-13s %10s %12s   %s%n", t.type(), t.amount(), t.balanceAfter(), t.description());
        }

        System.out.println(LINE);
        System.out.println("Closing balance: " + Ansi.bold(account.getBalance().toString()));
        System.out.println();
    }

    public void printSummary(List<Account> accounts) {
        System.out.println(LINE);
        System.out.println(Ansi.bold(String.format("%-10s %-28s %-12s %12s", "NUMBER", "TYPE", "OWNER", "BALANCE")));
        System.out.println(LINE);
        for (Account account : accounts) {
            System.out.printf("%-10s %-28s %-12s %12s%n",
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getOwnerName(),
                    account.getBalance());
        }
        System.out.println(LINE);
        System.out.println();
    }
}
