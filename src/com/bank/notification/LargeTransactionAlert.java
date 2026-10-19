package com.bank.notification;

import com.bank.console.Ansi;
import com.bank.transaction.Transaction;

import java.math.BigDecimal;

/** Raises an alert when a single transaction is bigger than a threshold. */
public class LargeTransactionAlert implements TransactionListener {

    private final BigDecimal threshold;

    public LargeTransactionAlert(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        if (transaction.amount().compareTo(threshold) > 0) {
            System.out.println("  " + Ansi.style("1;33", String.format(
                    "[ALERT]  Large %s of %s on account %s (limit %s)",
                    transaction.type(),
                    transaction.amount(),
                    transaction.accountNumber(),
                    threshold)));
        }
    }
}
