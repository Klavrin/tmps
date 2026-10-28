# Lab 1 — Creational Design Patterns

This lab applies **five creational design patterns** to the existing banking
domain. The goal is not to add new banking features, but to show how objects
are created, configured and reused in a controlled way.

All new code lives in `src/com/bank/creational/` and is wired into the
existing application. Run the walkthrough with:

```bash
./run.sh --creational
```

---

## 1. The domain

The domain is the same as lab 0: a small bank with savings, checking and
fixed-deposit accounts, interest rules, transactions and notifications.

The important entities are:

| Entity | Role |
|---|---|
| `Account` and its subclasses | the products that get created |
| `InterestPolicy` | a rule handed to an account at creation time |
| `TransactionListener` | a reaction registered on the bank |
| `AccountNumberGenerator` | the source of unique account numbers |
| `BankConfig` | application-wide settings |

---

## 2. Patterns at a glance

| # | Pattern | Problem it solves | Main classes |
|---|---|---|---|
| 1 | **Singleton** | The app must agree on one set of configuration values. | `BankConfig` |
| 2 | **Builder** | Accounts have many optional settings; constructors become unreadable. | `AccountBuilder`, `AccountType` |
| 3 | **Factory Method** | The client should not name concrete account classes. | `AccountFactory`, `SavingsAccountFactory`, `CheckingAccountFactory`, `FixedDepositAccountFactory`, `AccountSpec` |
| 4 | **Abstract Factory** | A bank tier needs a *consistent family* of related objects. | `BankFactory`, `StandardBankFactory`, `PremiumBankFactory` |
| 5 | **Prototype** | New accounts should be cloned from pre-configured templates. | `Account.copy(...)`, `AccountTemplateRegistry` |

---

## 3. Singleton — `BankConfig`

**File:** `src/com/bank/creational/singleton/BankConfig.java`

`BankConfig` holds the currency, the large-transaction threshold, the account
number prefix, the first account number and the default savings rate.

- The constructor is `private`.
- `getInstance()` creates the object lazily.
- **Double-checked locking** with a `volatile` field makes lazy creation safe
  for multiple threads.

**Why a Singleton here?** These values must be identical everywhere. If two
parts of the app had different thresholds, the same transaction could be
"large" in one place and "normal" in another.

**Where it is used:**

- `AccountNumberGenerator` reads the prefix and first number from it.
- `Main` and `GuidedDemo` take the large-transaction threshold from it.
- `PremiumBankFactory` uses the same threshold for its alert listener.

**Trade-off:** Singletons introduce global state and can make testing harder.
That is why this one holds only immutable configuration, not mutable business
data.

---

## 4. Builder — `AccountBuilder`

**File:** `src/com/bank/creational/builder/AccountBuilder.java`

Creating an account directly requires a constructor whose arguments are easy
to mix up:

```java
new SavingsAccount("ACC-1001", "Alice", new FixedRateInterest(3), new BigDecimal("100"));
```

The builder turns that into a readable chain:

```java
Account alice = new AccountBuilder("ACC-1001")
        .type(AccountType.SAVINGS)
        .owner("Alice")
        .interest(new FixedRateInterest(3.0))
        .minimumBalance(new BigDecimal("100"))
        .openingDeposit(new BigDecimal("1000"))
        .build();
```

- Optional settings have sensible defaults.
- Validation happens once, in `build()`.
- The type-specific constructor still checks its own rule (for example, a
  fixed deposit needs a term).
- `openingDeposit` is applied through the normal `deposit()` method, so the
  usual amount validation still runs.

**Trade-off:** More code than a constructor, but much easier to read and
extend when the number of settings grows.

---

## 5. Factory Method — `AccountFactory`

**Files:** `src/com/bank/creational/factory/`

`AccountFactory` is an abstract creator:

```java
public abstract class AccountFactory {
    public abstract String supportedType();
    public abstract Account create(AccountSpec spec);
}
```

Each subclass decides which concrete account to instantiate:

| Factory | Product |
|---|---|
| `SavingsAccountFactory` | `SavingsAccount` |
| `CheckingAccountFactory` | `CheckingAccount` |
| `FixedDepositAccountFactory` | `FixedDepositAccount` |

`AccountSpec` is a small record that carries everything a factory needs. It
keeps the method signature stable, so adding a new account type does not
change the base class or the client.

**Where it is used:** `BankCli.openAccount()` now selects a factory instead of
calling `new SavingsAccount(...)`, `new CheckingAccount(...)` or
`new FixedDepositAccount(...)` directly.

**Benefit:** the client depends on the abstraction, not on concrete classes.
Adding a `StudentAccountFactory` means one new subclass and no edits to the
client logic.

---

## 6. Abstract Factory — `BankFactory`

**Files:** `src/com/bank/creational/abstractfactory/`

A bank tier needs several objects that belong together:

- an account factory,
- a default interest policy,
- a notifier.

`BankFactory` creates that whole family:

```java
public interface BankFactory {
    String name();
    AccountFactory accountFactory();
    InterestPolicy defaultInterest();
    TransactionListener notifier();
}
```

| Family | Account factory | Default interest | Notifier |
|---|---|---|---|
| `StandardBankFactory` | savings | 3% per year | `ConsoleNotifier` |
| `PremiumBankFactory` | savings | 5% per year | `LargeTransactionAlert` |

**Why not create the pieces separately?** Because a client could accidentally
combine a premium interest rate with a standard notifier. The abstract factory
guarantees that the pieces are compatible.

**Benefit:** the client works only with `BankFactory`. It never names
`FixedRateInterest`, `ConsoleNotifier` or `LargeTransactionAlert`.

---

## 7. Prototype — `Account.copy(...)` and `AccountTemplateRegistry`

**Files:**

- `src/com/bank/account/Account.java` (the `copy` contract)
- `src/com/bank/account/SavingsAccount.java`
- `src/com/bank/account/CheckingAccount.java`
- `src/com/bank/account/FixedDepositAccount.java`
- `src/com/bank/creational/prototype/AccountTemplateRegistry.java`

`Account` declares:

```java
public abstract Account copy(String newAccountNumber, String newOwnerName);
```

Each concrete account implements it by constructing a new account with the
same settings but a new number and owner. The balance starts at zero.

`AccountTemplateRegistry` stores pre-configured prototype accounts and clones
them on demand:

```java
registry.register("student", studentPrototype);
Account clone = registry.createFrom("student", "New Student");
```

**Why a Prototype here?** Some account configurations are repeated for many
customers. Cloning a template is simpler and less error-prone than rebuilding
the configuration each time, and it keeps the configuration in one place.

**Trade-off:** a shallow copy would share mutable state. Here every field is
immutable (`BigDecimal`, `String`, `InterestPolicy` implementations), so the
copy is safe. If an account ever held a mutable list, `copy` would need to
clone it too.

---

## 8. How the patterns are wired into the existing project

| Existing class | Change | Pattern |
|---|---|---|
| `Account` | new abstract `copy(...)` method | Prototype |
| `SavingsAccount`, `CheckingAccount`, `FixedDepositAccount` | implement `copy(...)` | Prototype |
| `AccountNumberGenerator` | reads prefix and first number from `BankConfig` | Singleton |
| `Main` | `--creational` entry point; threshold from `BankConfig` | Singleton |
| `GuidedDemo` | threshold from `BankConfig` | Singleton |
| `BankCli.openAccount()` | creates accounts through `AccountFactory` | Factory Method |

Nothing in the original banking rules changed. The patterns only affect
**how objects are created**.

---

## 9. Running it

```bash
# the creational-patterns walkthrough
./run.sh --creational

# the original lab 0 walkthrough
./run.sh --demo

# compile by hand, if you prefer
javac -d out $(find src -name "*.java")
java -cp out com.bank.Main --creational
```

The walkthrough prints one section per pattern and shows the objects that were
created.

---

## 10. Class diagram

```
                         ┌──────────────────────┐
                         │      BankConfig      │  Singleton
                         │  getInstance()       │
                         └──────────┬───────────┘
                                    │ read by
                                    ▼
                         ┌──────────────────────┐
                         │ AccountNumberGenerator│
                         └──────────────────────┘

  Builder                  Factory Method                 Abstract Factory
┌───────────────┐      ┌──────────────────────┐      ┌──────────────────────┐
│ AccountBuilder│      │   AccountFactory     │      │     BankFactory      │
│  .type()      │      │  create(AccountSpec) │      │  accountFactory()    │
│  .owner()     │      └──────────┬───────────┘      │  defaultInterest()   │
│  .interest()  │                 │                  │  notifier()          │
│  .build()     │      ┌──────────┼──────────┐       └──────────┬───────────┘
└───────┬───────┘      ▼          ▼          ▼                  │
        │        Savings    Checking   FixedDeposit    Standard / Premium
        │        Factory    Factory    Factory          BankFactory
        │
        ▼
   ┌─────────┐
   │ Account │◄────────────── copy(...) ──────────────┐
   └────┬────┘                                          │
        │                                               │
   ┌────┴──────────────┐                        ┌───────┴──────────────┐
   ▼                   ▼                        │ AccountTemplateRegistry│
SavingsAccount   CheckingAccount                │  register(...)        │
   │                   │                        │  createFrom(...)      │
   └───────────┬───────┘                        └───────────────────────┘
               ▼
        FixedDepositAccount
```

---

## 11. Why these five patterns?

- **Singleton** — configuration genuinely must be unique.
- **Builder** — account construction has many optional, differently named
  settings.
- **Factory Method** — the app should not hard-code concrete account classes.
- **Abstract Factory** — bank tiers need consistent families of objects.
- **Prototype** — repeated account configurations are best cloned.

Together they cover the most common object-creation problems: *one shared
instance*, *complex construction*, *choosing a product*, *choosing a family of
products*, and *cloning a configured object*.

---

## 12. Possible extensions

- **Object Pool** — pool reusable `StringBuilder` objects used by `Screens` to
  avoid allocating a new one for every frame.
- **Factory registry** — map an account type name to its `AccountFactory` so
  the client does not need a `switch`.
- **More prototype templates** — add a `student`, `premium` or `business`
  template and clone it from the CLI.
