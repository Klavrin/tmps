package com.bank.cli;

import com.bank.Bank;
import com.bank.account.Account;
import com.bank.account.WithdrawableAccount;
import com.bank.console.Ansi;
import com.bank.console.Terminal;
import com.bank.console.Ui;
import com.bank.console.Ui.Option;
import com.bank.creational.factory.AccountFactory;
import com.bank.creational.factory.AccountSpec;
import com.bank.creational.factory.CheckingAccountFactory;
import com.bank.creational.factory.FixedDepositAccountFactory;
import com.bank.creational.factory.SavingsAccountFactory;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.InterestPolicy;
import com.bank.interest.NoInterest;
import com.bank.interest.TieredInterest;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * The interactive banking app: a dashboard plus a menu for every operation
 * the bank supports.
 *
 * <p><b>Single Responsibility:</b> this class only runs the conversation
 * with the user. Money moves through {@code TransactionService}, the
 * accounts enforce their own rules, and {@link Screens} does the drawing.
 * When an operation is refused, the account's own exception message is
 * shown - the app does not repeat any business rule.
 */
public class BankCli {

    private record MenuItem(String label, String detail, Runnable action) {
    }

    private final Bank bank;
    private final ActivityFeed feed;
    private final Terminal terminal;
    private final Runnable guidedDemo;
    private final Ui ui;
    private final Screens screens = new Screens();
    private final List<MenuItem> mainMenu = List.of(
            new MenuItem("Deposit", "put money into any account", this::deposit),
            new MenuItem("Withdraw", "take money out of savings or checking", this::withdraw),
            new MenuItem("Transfer", "move money between two accounts", this::transfer),
            new MenuItem("Month-end interest", "pay interest on every account", this::payInterest),
            new MenuItem("Statement", "full history of one account", this::showStatement),
            new MenuItem("Open account", "savings, checking or fixed deposit", this::openAccount),
            new MenuItem("Guided demo", "the scripted SOLID walkthrough", this::runGuidedDemo),
            new MenuItem("Quit", "", null));

    private String status = "";

    public BankCli(Bank bank, ActivityFeed feed, Terminal terminal, Runnable guidedDemo) {
        this.bank = bank;
        this.feed = feed;
        this.terminal = terminal;
        this.guidedDemo = guidedDemo;
        this.ui = new Ui(terminal);
    }

    public void run() {
        List<Option> options = mainMenu.stream().map(item -> new Option(item.label(), item.detail())).toList();
        while (true) {
            int choice = ui.choose(dashboard(), "What would you like to do?", options);
            if (choice < 0 || mainMenu.get(choice).action() == null) {
                return;
            }
            status = "";
            mainMenu.get(choice).action().run();
        }
    }

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    private void deposit() {
        Account account = pickAccount("Deposit into which account?", accounts(), BankCli::balanceOf);
        if (account == null) {
            return;
        }
        BigDecimal amount = askMoney("How much do you want to deposit into " + account.getAccountNumber() + "?");
        if (amount != null) {
            attempt(() -> bank.transactionService().deposit(account, amount),
                    "Deposited " + Screens.money(amount) + " into " + account.getAccountNumber());
        }
    }

    private void withdraw() {
        WithdrawableAccount account = pickAccount(
                "Withdraw from which account?  " + Ansi.dim("(fixed deposits can't be withdrawn from)"),
                withdrawableAccounts(), BankCli::availableOf);
        if (account == null) {
            return;
        }
        BigDecimal amount = askMoney("How much do you want to withdraw from " + account.getAccountNumber() + "?");
        if (amount != null) {
            attempt(() -> bank.transactionService().withdraw(account, amount),
                    "Withdrew " + Screens.money(amount) + " from " + account.getAccountNumber());
        }
    }

    private void transfer() {
        WithdrawableAccount from = pickAccount("Transfer from which account?", withdrawableAccounts(), BankCli::availableOf);
        if (from == null) {
            return;
        }
        List<Account> targets = accounts().stream().filter(a -> a != from).toList();
        Account to = pickAccount("Transfer from " + from.getAccountNumber() + " to which account?", targets, BankCli::balanceOf);
        if (to == null) {
            return;
        }
        BigDecimal amount = askMoney("How much do you want to move from "
                + from.getAccountNumber() + " to " + to.getAccountNumber() + "?");
        if (amount != null) {
            attempt(() -> bank.transactionService().transfer(from, to, amount),
                    "Moved " + Screens.money(amount) + " from " + from.getAccountNumber() + " to " + to.getAccountNumber());
        }
    }

    private void payInterest() {
        int before = feed.size();
        bank.interestService().applyMonthlyInterest();
        int paid = feed.size() - before;
        status = paid == 0
                ? Ansi.yellow("● No account earned interest this month")
                : Ansi.green("✔ Paid month-end interest on " + paid + (paid == 1 ? " account" : " accounts"));
    }

    private void showStatement() {
        Account account = pickAccount("Show the statement of which account?", accounts(), BankCli::balanceOf);
        if (account != null) {
            ui.pause(screens.header(accounts()) + screens.statement(account,
                    bank.transactionRepository().findByAccountNumber(account.getAccountNumber())));
        }
    }

    private void openAccount() {
        int type = ui.choose(dashboard(), "What kind of account?", List.of(
                new Option("Savings", "keeps a minimum balance, can withdraw the rest"),
                new Option("Checking", "everyday account that can go into overdraft"),
                new Option("Fixed deposit", "deposits only, money is locked for a term")));
        if (type < 0) {
            return;
        }
        String owner = ui.ask(dashboard(), "Who owns the new account?", "a name, e.g. Dave", null);
        if (owner == null) {
            return;
        }

        BigDecimal limit = null;
        Integer termInMonths = null;
        if (type == 0) {
            limit = askMoney("What minimum balance must " + owner + " keep?");
        } else if (type == 1) {
            limit = askMoney("How far may " + owner + " go into overdraft?");
        } else {
            termInMonths = askWholeNumber("How many months is the term?");
        }
        if (limit == null && termInMonths == null) {
            return;
        }
        InterestPolicy interest = askInterestPolicy();
        if (interest == null) {
            return;
        }

        Account account;
        try {
            AccountFactory factory = switch (type) {
                case 0 -> new SavingsAccountFactory();
                case 1 -> new CheckingAccountFactory();
                default -> new FixedDepositAccountFactory();
            };
            AccountSpec spec = new AccountSpec(
                    bank.numberGenerator().next(),
                    owner,
                    interest,
                    type == 0 ? limit : null,
                    type == 1 ? limit : null,
                    type == 2 ? termInMonths : null);
            account = factory.create(spec);
        } catch (IllegalArgumentException e) {
            status = Ansi.red("✖ " + e.getMessage());
            return;
        }
        bank.accountRepository().save(account);
        status = Ansi.green("✔ Opened " + account);

        BigDecimal opening = askMoney("Opening deposit for " + account.getAccountNumber() + "? (empty to skip)");
        if (opening != null) {
            attempt(() -> bank.transactionService().deposit(account, opening),
                    "Opened " + account + " with " + Screens.money(opening));
        }
    }

    /**
     * Open/Closed: the new account gets its interest rule handed in. Adding
     * another rule means a new {@link InterestPolicy} class and one more line here.
     */
    private InterestPolicy askInterestPolicy() {
        int choice = ui.choose(dashboard(), "Which interest rule?", List.of(
                new Option("No interest", "earns nothing"),
                new Option("Fixed rate", "the same yearly rate on any balance"),
                new Option("Tiered rate", "a higher rate above a threshold")));
        try {
            switch (choice) {
                case 0:
                    return new NoInterest();
                case 1: {
                    Double rate = askPercent("Yearly interest rate, in %?");
                    return rate == null ? null : new FixedRateInterest(rate);
                }
                case 2: {
                    BigDecimal threshold = askMoney("From what balance does the higher rate apply?");
                    Double lower = threshold == null ? null : askPercent("Yearly rate below " + Screens.money(threshold) + ", in %?");
                    Double upper = lower == null ? null : askPercent("Yearly rate from " + Screens.money(threshold) + " up, in %?");
                    return upper == null ? null : new TieredInterest(threshold, lower, upper);
                }
                default:
                    return null;
            }
        } catch (IllegalArgumentException e) {
            status = Ansi.red("✖ " + e.getMessage());
            return null;
        }
    }

    private void runGuidedDemo() {
        terminal.leaveFullScreen();
        guidedDemo.run();
        System.out.flush();
        ui.pauseAfterOutput("press any key to go back to the app");
        terminal.enterFullScreen();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String dashboard() {
        List<Account> accounts = accounts();
        String screen = screens.header(accounts) + screens.accountTable(accounts) + screens.activity(feed.latest(5));
        return status.isEmpty() ? screen : screen + "\n  " + status + "\n";
    }

    /** Runs a bank operation and reports either success or the account's own reason for refusing. */
    private void attempt(Runnable operation, String successMessage) {
        try {
            operation.run();
            status = Ansi.green("✔ " + successMessage);
        } catch (RuntimeException e) {
            status = Ansi.red("✖ " + e.getMessage());
        }
    }

    /** Works for any list of accounts; {@code detail} says what to show next to each one. */
    private <T extends Account> T pickAccount(String title, List<T> accounts, Function<T, String> detail) {
        if (accounts.isEmpty()) {
            status = Ansi.yellow("● There is no account to choose from");
            return null;
        }
        List<Option> options = accounts.stream().map(account -> new Option(
                String.format("%-9s %-10s %s", account.getAccountNumber(),
                        Ansi.fit(account.getOwnerName(), 10), account.getAccountType()),
                detail.apply(account)))
                .toList();
        int choice = ui.choose(dashboard(), title, options);
        return choice < 0 ? null : accounts.get(choice);
    }

    private BigDecimal askMoney(String question) {
        String error = null;
        while (true) {
            String answer = ui.ask(dashboard(), question, "an amount such as 250 or 99.95 · enter to confirm · esc to cancel", error);
            if (answer == null) {
                return null;
            }
            try {
                BigDecimal amount = new BigDecimal(answer.replace(",", ""));
                if (amount.stripTrailingZeros().scale() > 2) {
                    error = "Use at most two decimal places";
                } else {
                    return amount.setScale(2);
                }
            } catch (NumberFormatException e) {
                error = "\"" + answer + "\" is not an amount";
            }
        }
    }

    private Double askPercent(String question) {
        String error = null;
        while (true) {
            String answer = ui.ask(dashboard(), question, "a number such as 3 or 2.5 · enter to confirm · esc to cancel", error);
            if (answer == null) {
                return null;
            }
            try {
                double rate = Double.parseDouble(answer.replace("%", ""));
                if (Double.isFinite(rate)) {
                    return rate;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error below
            }
            error = "\"" + answer + "\" is not a percentage";
        }
    }

    private Integer askWholeNumber(String question) {
        String error = null;
        while (true) {
            String answer = ui.ask(dashboard(), question, "a whole number such as 12 · enter to confirm · esc to cancel", error);
            if (answer == null) {
                return null;
            }
            try {
                return Integer.parseInt(answer);
            } catch (NumberFormatException e) {
                error = "\"" + answer + "\" is not a whole number";
            }
        }
    }

    private static String balanceOf(Account account) {
        return "balance " + Screens.money(account.getBalance());
    }

    /** Every withdrawable subclass answers this one call correctly (LSP). */
    private static String availableOf(WithdrawableAccount account) {
        return "available " + Screens.money(account.getAvailableFunds());
    }

    private List<Account> accounts() {
        return bank.accountRepository().findAll();
    }

    private List<WithdrawableAccount> withdrawableAccounts() {
        return accounts().stream()
                .filter(WithdrawableAccount.class::isInstance)
                .map(WithdrawableAccount.class::cast)
                .toList();
    }
}
