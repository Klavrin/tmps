package com.bank.creational;

import com.bank.account.Account;
import com.bank.console.Ansi;
import com.bank.creational.abstractfactory.BankFactory;
import com.bank.creational.abstractfactory.PremiumBankFactory;
import com.bank.creational.abstractfactory.StandardBankFactory;
import com.bank.creational.builder.AccountBuilder;
import com.bank.creational.builder.AccountType;
import com.bank.creational.factory.AccountFactory;
import com.bank.creational.factory.AccountSpec;
import com.bank.creational.factory.CheckingAccountFactory;
import com.bank.creational.factory.FixedDepositAccountFactory;
import com.bank.creational.factory.SavingsAccountFactory;
import com.bank.creational.prototype.AccountTemplateRegistry;
import com.bank.creational.singleton.BankConfig;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.NoInterest;
import com.bank.interest.TieredInterest;
import com.bank.service.AccountNumberGenerator;

import java.math.BigDecimal;
import java.util.List;

/**
 * A scripted walkthrough of the five creational patterns used in lab 1.
 *
 * <p>Run it with {@code ./run.sh --creational}. Each section shows one pattern
 * creating real objects from the banking domain.
 */
public final class CreationalDemo {

    private CreationalDemo() {
    }

    public static void run() {
        AccountNumberGenerator numbers = new AccountNumberGenerator();

        singleton(numbers);
        builder(numbers);
        factoryMethod(numbers);
        abstractFactory(numbers);
        prototype(numbers);

        System.out.println();
        System.out.println("  " + Ansi.green("✔ Creational demo finished."));
    }

    /** 1. Singleton: one shared configuration object. */
    private static void singleton(AccountNumberGenerator numbers) {
        section("1. Singleton — one BankConfig for the whole app");

        BankConfig first = BankConfig.getInstance();
        BankConfig second = BankConfig.getInstance();

        System.out.println("  getInstance() returns the same object: "
                + Ansi.bold(String.valueOf(first == second)));
        System.out.println("  currency:                " + first.currencyCode());
        System.out.println("  large transaction limit: " + first.largeTransactionThreshold());
        System.out.println("  account number prefix:   " + first.accountNumberPrefix());
        System.out.println("  first account number:    " + first.firstAccountNumber());
        System.out.println("  AccountNumberGenerator reads the same config -> " + numbers.next());
    }

    /** 2. Builder: readable, step-by-step account construction. */
    private static void builder(AccountNumberGenerator numbers) {
        section("2. Builder — AccountBuilder assembles an account step by step");

        Account alice = new AccountBuilder(numbers.next())
                .type(AccountType.SAVINGS)
                .owner("Alice")
                .interest(new FixedRateInterest(3.0))
                .minimumBalance(money("100"))
                .openingDeposit(money("1000"))
                .build();

        Account carol = new AccountBuilder(numbers.next())
                .type(AccountType.FIXED_DEPOSIT)
                .owner("Carol")
                .interest(new TieredInterest(money("10000"), 4.0, 5.0))
                .termInMonths(12)
                .openingDeposit(money("15000"))
                .build();

        describe(alice);
        describe(carol);
    }

    /** 3. Factory Method: each subclass decides the concrete account. */
    private static void factoryMethod(AccountNumberGenerator numbers) {
        section("3. Factory Method — each factory creates its own account type");

        List<AccountFactory> factories = List.of(
                new SavingsAccountFactory(),
                new CheckingAccountFactory(),
                new FixedDepositAccountFactory());

        for (AccountFactory factory : factories) {
            AccountSpec spec = specFor(factory.supportedType(), numbers.next());
            describe(factory.create(spec));
        }
    }

    /** 4. Abstract Factory: a consistent family of related objects. */
    private static void abstractFactory(AccountNumberGenerator numbers) {
        section("4. Abstract Factory — one family per bank tier");

        for (BankFactory factory : List.of(new StandardBankFactory(), new PremiumBankFactory())) {
            AccountSpec spec = new AccountSpec(
                    numbers.next(),
                    "Customer of " + factory.name(),
                    factory.defaultInterest(),
                    money("100"),
                    null,
                    null);

            Account account = factory.accountFactory().create(spec);
            System.out.println("  " + Ansi.bold(factory.name()) + " -> " + account
                    + Ansi.dim(" | interest ") + account.getInterestPolicy().describe());
            System.out.println("  " + Ansi.dim("  notifier: ")
                    + factory.notifier().getClass().getSimpleName());
        }
    }

    /** 5. Prototype: clone pre-configured account templates. */
    private static void prototype(AccountNumberGenerator numbers) {
        section("5. Prototype — clone pre-configured account templates");

        AccountTemplateRegistry registry = new AccountTemplateRegistry(numbers);
        registry.register("student", new AccountBuilder(numbers.next())
                .type(AccountType.SAVINGS)
                .owner("template")
                .interest(new FixedRateInterest(1.0))
                .minimumBalance(money("0"))
                .build());
        registry.register("premium", new AccountBuilder(numbers.next())
                .type(AccountType.SAVINGS)
                .owner("template")
                .interest(new TieredInterest(money("10000"), 4.0, 5.0))
                .minimumBalance(money("500"))
                .build());

        System.out.println("  registered templates: " + registry.keys());
        for (String key : List.of("student", "premium")) {
            describe(registry.createFrom(key, "New " + key + " customer"));
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static AccountSpec specFor(String type, String number) {
        return switch (type) {
            case "Savings" -> new AccountSpec(
                    number, "Customer", new FixedRateInterest(3.0), money("100"), null, null);
            case "Checking" -> new AccountSpec(
                    number, "Customer", new NoInterest(), null, money("500"), null);
            case "Fixed Deposit" -> new AccountSpec(
                    number, "Customer", new FixedRateInterest(4.0), null, null, 12);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    private static void describe(Account account) {
        System.out.println("  created: " + Ansi.bold(account.toString())
                + Ansi.dim(" | balance ") + account.getBalance()
                + Ansi.dim(" | interest ") + account.getInterestPolicy().describe());
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(Ansi.style("1;36", "◆ " + title));
    }
}
