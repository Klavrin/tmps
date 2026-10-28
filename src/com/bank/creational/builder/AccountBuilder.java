package com.bank.creational.builder;

import com.bank.account.Account;
import com.bank.account.CheckingAccount;
import com.bank.account.FixedDepositAccount;
import com.bank.account.SavingsAccount;
import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Builds an {@link Account} step by step.
 *
 * <p><b>Creational pattern: Builder.</b> Accounts have several optional
 * settings (minimum balance, overdraft limit, term, opening deposit) and the
 * constructors differ from one account type to another. Calling those
 * constructors directly produces "telescoping" code where the meaning of each
 * argument is hard to read:
 *
 * <pre>{@code
 * new SavingsAccount("ACC-1001", "Alice", new FixedRateInterest(3), new BigDecimal("100"));
 * }</pre>
 *
 * <p>The builder replaces that with a readable chain:
 *
 * <pre>{@code
 * new AccountBuilder("ACC-1001")
 *         .type(AccountType.SAVINGS)
 *         .owner("Alice")
 *         .interest(new FixedRateInterest(3))
 *         .minimumBalance(new BigDecimal("100"))
 *         .openingDeposit(new BigDecimal("1000"))
 *         .build();
 * }</pre>
 *
 * <p>All validation is collected in {@link #build()}, so an invalid
 * combination fails in one place instead of halfway through construction.
 */
public final class AccountBuilder {

    private final String accountNumber;
    private AccountType type = AccountType.SAVINGS;
    private String ownerName;
    private InterestPolicy interestPolicy;
    private BigDecimal minimumBalance = BigDecimal.ZERO;
    private BigDecimal overdraftLimit = BigDecimal.ZERO;
    private Integer termInMonths;
    private BigDecimal openingDeposit;

    public AccountBuilder(String accountNumber) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
    }

    public AccountBuilder type(AccountType type) {
        this.type = Objects.requireNonNull(type, "type");
        return this;
    }

    public AccountBuilder owner(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public AccountBuilder interest(InterestPolicy interestPolicy) {
        this.interestPolicy = interestPolicy;
        return this;
    }

    public AccountBuilder minimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
        return this;
    }

    public AccountBuilder overdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
        return this;
    }

    public AccountBuilder termInMonths(int termInMonths) {
        this.termInMonths = termInMonths;
        return this;
    }

    public AccountBuilder openingDeposit(BigDecimal openingDeposit) {
        this.openingDeposit = openingDeposit;
        return this;
    }

    /**
     * Creates the account. Required fields are checked first, then the
     * type-specific constructor validates its own setting.
     */
    public Account build() {
        require(ownerName != null && !ownerName.isBlank(), "an owner name is required");
        require(interestPolicy != null, "an interest policy is required");

        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(
                    accountNumber, ownerName, interestPolicy,
                    nonNegative(minimumBalance, "minimum balance"));
            case CHECKING -> new CheckingAccount(
                    accountNumber, ownerName, interestPolicy,
                    nonNegative(overdraftLimit, "overdraft limit"));
            case FIXED_DEPOSIT -> new FixedDepositAccount(
                    accountNumber, ownerName, interestPolicy, requiredTerm());
        };

        if (openingDeposit != null && openingDeposit.signum() > 0) {
            account.deposit(openingDeposit);
        }
        return account;
    }

    private int requiredTerm() {
        require(termInMonths != null, "a term in months is required for a fixed deposit");
        return termInMonths;
    }

    private static BigDecimal nonNegative(BigDecimal value, String name) {
        require(value != null && value.signum() >= 0, name + " cannot be negative");
        return value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("AccountBuilder: " + message);
        }
    }
}
