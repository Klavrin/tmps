package com.bank.service;

import com.bank.account.Account;
import com.bank.account.WithdrawableAccount;
import com.bank.notification.TransactionListener;
import com.bank.repository.TransactionRepository;
import com.bank.transaction.Transaction;
import com.bank.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves money in, out and between accounts, and records each movement.
 *
 * <p>
 * <b>Single Responsibility:</b> this class coordinates money movements.
 * The balance rules live in the accounts, storage lives in the repository
 * and reactions (alerts, notices) live in listeners.
 *
 * <p>
 * <b>Open/Closed:</b> new reactions are added with
 * {@link #addListener(TransactionListener)} - this class never changes.
 *
 * <p>
 * <b>Liskov Substitution:</b> the method signatures do the work.
 * {@code withdraw} and {@code transfer} only accept a
 * {@link WithdrawableAccount}, so they work with ANY account that can be
 * withdrawn from, and you cannot accidentally pass a fixed deposit.
 * {@code deposit} accepts any {@link Account}, because every account
 * supports deposits.
 */
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final List<TransactionListener> listeners = new ArrayList<>();

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public void addListener(TransactionListener listener) {
    listeners.add(listener);
  }

  public void deposit(Account account, BigDecimal amount) {
    account.deposit(amount);
    record(TransactionType.DEPOSIT, account, amount, "Cash deposit");
  }

  public void withdraw(WithdrawableAccount account, BigDecimal amount) {
    account.withdraw(amount);
    record(TransactionType.WITHDRAWAL, account, amount, "Cash withdrawal");
  }

  public void transfer(WithdrawableAccount from, Account to, BigDecimal amount) {
    if (from == to) {
      throw new IllegalArgumentException("Cannot transfer to the same account");
    }
    // Withdraw first: if there is not enough money it throws,
    // and nothing has changed on either account.
    from.withdraw(amount);
    to.deposit(amount);

    record(TransactionType.TRANSFER_OUT, from, amount, "Transfer to " + to.getAccountNumber());
    record(TransactionType.TRANSFER_IN, to, amount, "Transfer from " + from.getAccountNumber());
  }

  public void payInterest(Account account, BigDecimal amount) {
    account.deposit(amount);
    record(TransactionType.INTEREST, account, amount,
        "Monthly interest (" + account.getInterestPolicy().describe() + ")");
  }

  private void record(TransactionType type, Account account, BigDecimal amount, String description) {
    Transaction transaction = new Transaction(
        type,
        account.getAccountNumber(),
        amount,
        account.getBalance(),
        description,
        LocalDateTime.now());

    transactionRepository.save(transaction);
    listeners.forEach(listener -> listener.onTransaction(transaction));
  }
}
