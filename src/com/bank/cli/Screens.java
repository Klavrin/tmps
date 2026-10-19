package com.bank.cli;

import com.bank.account.Account;
import com.bank.account.WithdrawableAccount;
import com.bank.console.Ansi;
import com.bank.transaction.Transaction;
import com.bank.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * <b>Single Responsibility:</b> turns accounts and transactions into
 * coloured blocks of terminal text. It never reads input or moves money.
 */
final class Screens {

    static final int WIDTH = 76;
    private static final int STATEMENT_ROWS = 12;

    String header(List<Account> accounts) {
        BigDecimal total = accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        String left = Ansi.style("1;36", "◆ SOLID BANK") + Ansi.dim("  S · O · L principles");
        String right = Ansi.dim(accounts.size() + " accounts  total ") + Ansi.bold(money(total));
        int gap = WIDTH - 4 - Ansi.visibleLength(left) - Ansi.visibleLength(right);
        String border = "─".repeat(WIDTH - 2);

        return "\n  " + Ansi.cyan("╭" + border + "╮") + "\n"
                + "  " + Ansi.cyan("│") + " " + left + " ".repeat(Math.max(1, gap)) + right + " " + Ansi.cyan("│") + "\n"
                + "  " + Ansi.cyan("╰" + border + "╯") + "\n";
    }

    String accountTable(List<Account> accounts) {
        StringBuilder table = new StringBuilder("\n  ")
                .append(Ansi.dim(String.format("%-10s%-27s%-12s%13s%13s",
                        "NUMBER", "TYPE", "OWNER", "BALANCE", "AVAILABLE")))
                .append('\n');
        for (Account account : accounts) {
            table.append("  ")
                    .append(Ansi.bold(String.format("%-10s", account.getAccountNumber())))
                    .append(String.format("%-27s%-12s",
                            Ansi.fit(account.getAccountType(), 26), Ansi.fit(account.getOwnerName(), 11)))
                    .append(balance(String.format("%13s", money(account.getBalance())), account.getBalance()))
                    .append(available(account))
                    .append('\n');
        }
        return table.toString();
    }

    String activity(List<ActivityFeed.Entry> entries) {
        StringBuilder feed = new StringBuilder("\n  ").append(Ansi.dim("RECENT ACTIVITY")).append('\n');
        if (entries.isEmpty()) {
            feed.append("  ").append(Ansi.dim("nothing yet")).append('\n');
        }
        for (ActivityFeed.Entry entry : entries) {
            Transaction t = entry.transaction();
            boolean inflow = isInflow(t.type());
            String amount = String.format("%13s", (inflow ? "+" : "-") + money(t.amount()));
            feed.append("  ")
                    .append(inflow ? Ansi.green("▲") : Ansi.red("▼")).append(' ')
                    .append(String.format("%-10s%-14s", t.accountNumber(), label(t.type())))
                    .append(inflow ? Ansi.green(amount) : Ansi.red(amount))
                    .append(Ansi.dim("   balance "))
                    .append(money(t.balanceAfter()));
            if (entry.large()) {
                feed.append("  ").append(Ansi.style("1;33", "⚠ large"));
            }
            feed.append('\n');
        }
        return feed.toString();
    }

    String statement(Account account, List<Transaction> transactions) {
        String rule = "  " + Ansi.dim("─".repeat(WIDTH - 2)) + "\n";
        StringBuilder statement = new StringBuilder("\n  ")
                .append(Ansi.style("1;36", "STATEMENT")).append("  ").append(Ansi.bold(account.toString())).append('\n')
                .append("  ").append(Ansi.dim("interest   ")).append(account.getInterestPolicy().describe()).append('\n')
                .append(rule)
                .append("  ").append(Ansi.dim(String.format("%-14s%13s%13s   %s", "TYPE", "AMOUNT", "BALANCE", "DESCRIPTION")))
                .append('\n');

        if (transactions.isEmpty()) {
            statement.append("  ").append(Ansi.dim("no transactions yet")).append('\n');
        }
        int hidden = Math.max(0, transactions.size() - STATEMENT_ROWS);
        if (hidden > 0) {
            statement.append("  ").append(Ansi.dim("… " + hidden + " earlier transactions not shown")).append('\n');
        }
        for (Transaction t : transactions.subList(hidden, transactions.size())) {
            boolean inflow = isInflow(t.type());
            String amount = String.format("%13s", (inflow ? "+" : "-") + money(t.amount()));
            statement.append("  ")
                    .append(String.format("%-14s", label(t.type())))
                    .append(inflow ? Ansi.green(amount) : Ansi.red(amount))
                    .append(balance(String.format("%13s", money(t.balanceAfter())), t.balanceAfter()))
                    .append("   ")
                    .append(Ansi.dim(Ansi.fit(t.description(), WIDTH - 45)))
                    .append('\n');
        }

        return statement.append(rule)
                .append("  ").append(Ansi.bold(String.format("%-14s", "Closing")))
                .append(" ".repeat(13))
                .append(balance(String.format("%13s", money(account.getBalance())), account.getBalance()))
                .append('\n')
                .toString();
    }

    static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }

    /** Bold, and red when the account is overdrawn. */
    private static String balance(String text, BigDecimal amount) {
        return amount.signum() < 0 ? Ansi.style("1;31", text) : Ansi.bold(text);
    }

    /** Only accounts that allow withdrawals have "available funds" (LSP: no fake answers). */
    private static String available(Account account) {
        if (account instanceof WithdrawableAccount withdrawable) {
            return Ansi.green(String.format("%13s", money(withdrawable.getAvailableFunds())));
        }
        return Ansi.dim(String.format("%13s", "locked"));
    }

    private static boolean isInflow(TransactionType type) {
        return switch (type) {
            case DEPOSIT, TRANSFER_IN, INTEREST -> true;
            case WITHDRAWAL, TRANSFER_OUT -> false;
        };
    }

    private static String label(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "Deposit";
            case WITHDRAWAL -> "Withdrawal";
            case TRANSFER_IN -> "Transfer in";
            case TRANSFER_OUT -> "Transfer out";
            case INTEREST -> "Interest";
        };
    }
}
