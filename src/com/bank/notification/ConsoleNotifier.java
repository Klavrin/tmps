package com.bank.notification;

import com.bank.console.Ansi;
import com.bank.transaction.Transaction;

/** Prints a short notice for every transaction, like a banking-app push message. */
public class ConsoleNotifier implements TransactionListener {

    @Override
    public void onTransaction(Transaction transaction) {
        System.out.println("  " + Ansi.cyan("[notice]") + " "
                + String.format("%s %-12s %10s", transaction.accountNumber(), transaction.type(), transaction.amount())
                + Ansi.dim("  -> balance ") + Ansi.bold(transaction.balanceAfter().toString()));
    }
}
