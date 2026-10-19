package com.bank;

import com.bank.account.Account;
import com.bank.account.CheckingAccount;
import com.bank.account.FixedDepositAccount;
import com.bank.account.SavingsAccount;
import com.bank.account.WithdrawableAccount;
import com.bank.console.Ansi;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.NoInterest;
import com.bank.interest.TieredInterest;
import com.bank.notification.ConsoleNotifier;
import com.bank.notification.LargeTransactionAlert;
import com.bank.report.StatementPrinter;

import java.math.BigDecimal;
import java.util.List;

/**
 * A scripted walkthrough that shows each SOLID principle in action, on a
 * fresh bank. Run it with {@code ./run.sh --demo} or from the app's menu.
 */
public final class GuidedDemo {

    private GuidedDemo() {
    }

    public static void run() {
        // ---------------------------------------------------------------
        // 1. Wire the system together. Each object has ONE job (SRP).
        // ---------------------------------------------------------------
        Bank bank = Bank.create();
        StatementPrinter printer = new StatementPrinter(bank.transactionRepository());

        // Open/Closed: new behaviour is PLUGGED IN, not coded into the service.
        bank.transactionService().addListener(new ConsoleNotifier());
        bank.transactionService().addListener(new LargeTransactionAlert(money("10000")));

        // ---------------------------------------------------------------
        // 2. Open accounts. Each one gets its interest rule from outside (OCP).
        // ---------------------------------------------------------------
        SavingsAccount alice = new SavingsAccount(
                bank.numberGenerator().next(), "Alice", new FixedRateInterest(3.0), money("100"));
        CheckingAccount bob = new CheckingAccount(
                bank.numberGenerator().next(), "Bob", new NoInterest(), money("500"));
        FixedDepositAccount carol = new FixedDepositAccount(
                bank.numberGenerator().next(), "Carol", new TieredInterest(money("10000"), 4.0, 5.0), 12);

        bank.accountRepository().save(alice);
        bank.accountRepository().save(bob);
        bank.accountRepository().save(carol);

        section("Opening deposits");
        bank.transactionService().deposit(alice, money("1000"));
        bank.transactionService().deposit(bob, money("200"));
        bank.transactionService().deposit(carol, money("15000"));   // triggers the large-transaction alert

        section("Withdrawals and transfers");
        bank.transactionService().withdraw(bob, money("600"));      // allowed: Bob has a 500 overdraft
        bank.transactionService().transfer(alice, bob, money("300"));

        section("Rules are enforced by the accounts themselves");
        try {
            bank.transactionService().withdraw(alice, money("1000")); // Alice must keep 100 in savings
        } catch (InsufficientFundsException e) {
            rejected(e.getMessage());
        }
        try {
            bank.transactionService().deposit(bob, money("-50"));
        } catch (InvalidAmountException e) {
            rejected(e.getMessage());
        }
        // bank.transactionService().withdraw(carol, money("10"));
        //   ^ Uncomment this line and the code will NOT compile.
        //     A fixed deposit is not a WithdrawableAccount, so the type system
        //     stops the mistake before the program even runs (LSP).

        section("Liskov Substitution: one method, many account types");
        showWithdrawalLimits(List.of(alice, bob), money("250"));

        section("Month-end interest (one loop, every account type)");
        bank.interestService().applyMonthlyInterest();

        section("Account summary");
        printer.printSummary(bank.accountRepository().findAll());

        section("Statements");
        for (Account account : bank.accountRepository().findAll()) {
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
            boolean canWithdraw = account.canWithdraw(amount);
            System.out.printf("  %-28s balance %9s %s available %9s %s can withdraw %s? %s%n",
                    account,
                    account.getBalance(),
                    Ansi.dim("|"),
                    account.getAvailableFunds(),
                    Ansi.dim("|"),
                    amount,
                    canWithdraw ? Ansi.green("yes") : Ansi.red("no"));
        }
    }

    private static void rejected(String reason) {
        System.out.println("  " + Ansi.style("1;31", "✖ Rejected:") + " " + reason);
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(Ansi.style("1;36", "◆ " + title));
    }
}
