package com.bank.notification;

import com.bank.transaction.Transaction;

/**
 * Something that wants to react whenever a transaction happens
 * (send an SMS, write an audit log, flag fraud, ...).
 *
 * <p><b>Open/Closed:</b> {@code TransactionService} just loops over its
 * listeners and calls this method. To add a new reaction you write a new
 * listener class - you never edit {@code TransactionService}.
 */
public interface TransactionListener {

    void onTransaction(Transaction transaction);
}
