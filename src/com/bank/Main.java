package com.bank;

import com.bank.account.Account;
import com.bank.account.CheckingAccount;
import com.bank.account.FixedDepositAccount;
import com.bank.account.SavingsAccount;
import com.bank.account.WithdrawableAccount;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.NoInterest;
import com.bank.interest.TieredInterest;
import com.bank.notification.ConsoleNotifier;
import com.bank.notification.LargeTransactionAlert;
import com.bank.report.StatementPrinter;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountNumberGenerator;
import com.bank.service.InterestService;
import com.bank.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    AccountRepository accountRepository = new AccountRepository();
    TransactionRepository transactionRepository = new TransactionRepository();
    AccountNumberGenerator numberGenerator = new AccountNumberGenerator();
    TransactionService transactionService = new TransactionService(transactionRepository);
    InterestService interestService = new InterestService(accountRepository, transactionService);
    StatementPrinter printer = new StatementPrinter(transactionRepository);

    // new behaviour is PLUGGED IN, not coded into the service - open/close
    // principle
    transactionService.addListener(new ConsoleNotifier());
    transactionService.addListener(new LargeTransactionAlert(money("10000")));

    // open accounts
    SavingsAccount alice = new SavingsAccount(
        numberGenerator.next(), "Alice", new FixedRateInterest(3.0), money("100"));
    CheckingAccount bob = new CheckingAccount(
        numberGenerator.next(), "Bob", new NoInterest(), money("500"));
    FixedDepositAccount carol = new FixedDepositAccount(
        numberGenerator.next(), "Carol", new TieredInterest(money("10000"), 4.0, 5.0), 12);

    accountRepository.save(alice);
    accountRepository.save(bob);
    accountRepository.save(carol);

    section("Opening deposits");
    transactionService.deposit(alice, money("1000"));
    transactionService.deposit(bob, money("200"));
    transactionService.deposit(carol, money("15000"));

    section("Withdrawals and transfers");
    transactionService.withdraw(bob, money("600"));
    transactionService.transfer(alice, bob, money("300"));

    section("Rules are enforced by the accounts themselves");
    try {
      transactionService.withdraw(alice, money("1000"));
    } catch (InsufficientFundsException e) {
      System.out.println("  Rejected: " + e.getMessage());
    }
    try {
      transactionService.deposit(bob, money("-50"));
    } catch (InvalidAmountException e) {
      System.out.println("  Rejected: " + e.getMessage());
    }
    // transactionService.withdraw(carol, money("10"));
    // ^ Uncomment this line and the code will NOT compile.
    // A fixed deposit is not a WithdrawableAccount, so the type system
    // stops the mistake before the program even runs (LSP).

    section("Liskov Substitution: one method, many account types");
    showWithdrawalLimits(List.of(alice, bob), money("250"));

    section("Month-end interest (one loop, every account type)");
    interestService.applyMonthlyInterest();

    section("Account summary");
    printer.printSummary(accountRepository.findAll());

    section("Statements");
    for (Account account : accountRepository.findAll()) {
      printer.printStatement(account);
    }
  }

  /**
   * This method knows nothing about savings or checking accounts. It only
   * uses the {@link WithdrawableAccount} contract, and every subclass honours
   * that contract, so any of them can be passed in. That is LSP.
   */
  private static void showWithdrawalLimits(List<WithdrawableAccount> accounts, BigDecimal amount) {
    for (WithdrawableAccount account : accounts) {
      System.out.printf("  %-28s balance %9s | available %9s | can withdraw %s? %s%n",
          account,
          account.getBalance(),
          account.getAvailableFunds(),
          amount,
          account.canWithdraw(amount) ? "yes" : "no");
    }
  }

  private static BigDecimal money(String amount) {
    return new BigDecimal(amount).setScale(2);
  }

  private static void section(String title) {
    System.out.println();
    System.out.println("=== " + title + " ===");
  }
}
