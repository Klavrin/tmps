# Banking System — SOLID Principles (S, O, L)

A small banking system written in plain Java. It shows the first three SOLID principles:

| Letter | Principle | One-line meaning |
|--------|-----------|------------------|
| **S** | Single Responsibility | A class should have only one reason to change. |
| **O** | Open/Closed | You add new behaviour by writing new code, not by editing working code. |
| **L** | Liskov Substitution | A subclass must work anywhere its parent class is expected, without surprises. |

The program is an interactive, coloured terminal app. It starts with three sample accounts (savings, checking and fixed deposit) and shows them on a live dashboard. From a menu you drive with the arrow keys you can deposit, withdraw, transfer, pay month-end interest, view statements and open new accounts. When an operation breaks a rule, the account's own reason is shown.

The original scripted walkthrough is still there as the **Guided demo** menu entry, or run it directly with `./run.sh --demo`.

---

## Requirements

- **Java 17 or newer** (the code uses `record` and `Stream.toList()`).
  This project was developed and tested with **OpenJDK 25**.
- No Maven, Gradle or external libraries are needed.

Check your version:

```bash
java -version
javac -version
```

---

## How to run

### Option 1: the script (macOS / Linux)

```bash
./run.sh
```

The script deletes any old build, compiles every file in `src/` into `out/` and starts the app. Use `./run.sh --demo` for the scripted walkthrough.

**Controls:** `↑`/`↓` (or `j`/`k`) to move, `Enter` to select, `1`–`9` to jump to an entry, `Esc` (or `q`) to go back or quit. When you type an amount, `Esc` or an empty answer cancels.

Arrow keys need a real terminal (macOS / Linux, using `stty`). On Windows, in most IDE consoles or with piped input, the app switches to numbered menus automatically: type the number and press Enter. Set `NO_COLOR=1` to turn colours off.

### Option 2: by hand (macOS / Linux)

```bash
javac -d out $(find src -name "*.java")
java -cp out com.bank.Main
```

### Option 3: Windows (PowerShell)

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out com.bank.Main
```

### Option 4: an IDE (IntelliJ IDEA / VS Code / Eclipse)

Open the project folder, mark `src` as the source root and run `com.bank.Main`. IDE consoles usually can't send single key presses, so you'll get the numbered menus. Run `./run.sh` in a real terminal to use the arrow keys.

---

## Project structure

```
src/com/bank/
├── Main.java                         Starts the app (or the demo with --demo)
├── Bank.java                         Wires the repositories and services together
├── GuidedDemo.java                   The scripted SOLID walkthrough
├── cli/
│   ├── BankCli.java                  The interactive app: menus and operations
│   ├── Screens.java                  Draws the dashboard, tables and statements
│   └── ActivityFeed.java             Listener that remembers recent transactions
├── console/
│   ├── Terminal.java                 Raw key input, screen drawing (no bank code)
│   ├── Ui.java                       Arrow-key menus, text prompts, numbered fallback
│   └── Ansi.java                     Colours and text padding
├── account/
│   ├── Account.java                  Base class: deposit, balance, interest (every account)
│   ├── WithdrawableAccount.java      Adds withdraw() for accounts that allow it
│   ├── SavingsAccount.java           Must keep a minimum balance
│   ├── CheckingAccount.java          Can go into overdraft up to a limit
│   └── FixedDepositAccount.java      Deposits only, no withdrawals
├── interest/
│   ├── InterestPolicy.java           Interface: "how much interest this month?"
│   ├── NoInterest.java
│   ├── FixedRateInterest.java        e.g. 3% per year
│   └── TieredInterest.java           Lower rate below a threshold, higher above
├── transaction/
│   ├── Transaction.java              Immutable record of one money movement
│   └── TransactionType.java          DEPOSIT, WITHDRAWAL, TRANSFER_IN, ...
├── notification/
│   ├── TransactionListener.java      Interface: "react to a transaction"
│   ├── ConsoleNotifier.java          Prints a notice for every transaction
│   └── LargeTransactionAlert.java    Warns about transactions above a limit
├── repository/
│   ├── AccountRepository.java        Stores and finds accounts
│   └── TransactionRepository.java    Stores and finds transactions
├── service/
│   ├── AccountNumberGenerator.java   Creates ACC-1001, ACC-1002, ...
│   ├── TransactionService.java       Deposit / withdraw / transfer / pay interest
│   └── InterestService.java          Month-end interest for all accounts
├── report/
│   └── StatementPrinter.java         Formats statements and summaries
└── exception/
    ├── InsufficientFundsException.java
    └── InvalidAmountException.java
```

---

## How each principle is applied

### S — Single Responsibility Principle

> *"A class should have one, and only one, reason to change."*

Each class does one job, so a change in one area only touches one class:

| If this changes... | ...only this class changes |
|---|---|
| The rules for an account's balance | `SavingsAccount`, `CheckingAccount`, ... |
| Where accounts are stored (e.g. a database) | `AccountRepository` |
| Where transactions are stored | `TransactionRepository` |
| How statements look (text, HTML, PDF) | `StatementPrinter` |
| The account number format | `AccountNumberGenerator` |
| How money moves between accounts | `TransactionService` |

A common mistake is a single `Account` class that holds the balance, stores its history, prints statements and sends emails. That class would change for four different reasons. Here those jobs are split up. For example, `Account` never calls `System.out.println`.

### O — Open/Closed Principle

> *"Software should be open for extension, but closed for modification."*

There are two extension points, and both are interfaces:

**1. `InterestPolicy`**: each account receives its interest rule when it is created.

```java
new SavingsAccount(number, "Alice", new FixedRateInterest(3.0), minimumBalance);
new FixedDepositAccount(number, "Carol", new TieredInterest(threshold, 4.0, 5.0), 12);
```

`InterestService` just calls `account.calculateMonthlyInterest()`. It has no `switch` on the account type. To add a new rule, such as a promotional rate, write a new class:

```java
public class PromotionalInterest implements InterestPolicy {
    public BigDecimal monthlyInterestFor(BigDecimal balance) { ... }
    public String describe() { return "promo rate"; }
}
```

No existing class needs to change.

**2. `TransactionListener`**: `TransactionService` notifies every registered listener after each transaction.

```java
transactionService.addListener(new ConsoleNotifier());
transactionService.addListener(new LargeTransactionAlert(money("10000")));
```

To add an SMS sender, an audit log or a fraud check, write a new listener and register it. `TransactionService` stays untouched. The interactive app proves it: its live "recent activity" panel is `ActivityFeed`, just one more listener.

New **account types** are also an extension. `InterestService`, `StatementPrinter` and `TransactionService` work with `Account` and `WithdrawableAccount`, so a new `StudentAccount extends WithdrawableAccount` works with all of them right away.

### L — Liskov Substitution Principle

> *"Objects of a subclass must be usable wherever the parent class is expected, without breaking the program."*

**The classic mistake:** put `withdraw()` on `Account`. A fixed deposit can't withdraw, so `FixedDepositAccount` would have to override it and throw `UnsupportedOperationException`. Any code that loops over a `List<Account>` and calls `withdraw()` would then crash on a fixed deposit. The subclass is **not** substitutable.

**What this project does instead:**

```
                 Account                      deposit(), getBalance(), calculateMonthlyInterest()
                /       \
WithdrawableAccount    FixedDepositAccount     + withdraw(), canWithdraw(), getAvailableFunds()
      /        \
SavingsAccount  CheckingAccount
```

1. **Each parent promises only what all its children can do.** `Account` promises deposits. Only `WithdrawableAccount` promises withdrawals. `FixedDepositAccount` extends `Account`, so it never makes a promise it can't keep.

2. **Subclasses can't change the rules.** `deposit()` and `withdraw()` are `final`. Subclasses only answer one question, `getAvailableFunds()`:
   - Savings: `balance - minimumBalance`
   - Checking: `balance + overdraftLimit`

   Every `WithdrawableAccount` therefore follows the same contract: *"if `amount <= getAvailableFunds()` the withdrawal succeeds, otherwise `InsufficientFundsException` is thrown and nothing changes."*

3. **The compiler enforces it.** `TransactionService.withdraw(WithdrawableAccount, ...)` won't accept a `FixedDepositAccount`. Try uncommenting this line in `GuidedDemo.java`:

   ```java
   // bank.transactionService().withdraw(carol, money("10"));
   ```

   The app works the same way. Its *Withdraw* and *Transfer from* menus only list `WithdrawableAccount`s, so a fixed deposit never appears there.

   You will get a compile error, not a runtime crash:
   `incompatible types: FixedDepositAccount cannot be converted to WithdrawableAccount`.

4. **Substitution in practice.** `GuidedDemo.showWithdrawalLimits(List<WithdrawableAccount>)` handles savings and checking accounts the same way, without any `instanceof`. `InterestService` does the same with every `Account`.

---

## Guided demo output (`./run.sh --demo`, abridged, shown without colours)

```
◆ Opening deposits
  [notice] ACC-1001 DEPOSIT         1000.00  -> balance 1000.00
  [notice] ACC-1002 DEPOSIT          200.00  -> balance 200.00
  [notice] ACC-1003 DEPOSIT        15000.00  -> balance 15000.00
  [ALERT]  Large DEPOSIT of 15000.00 on account ACC-1003 (limit 10000.00)

◆ Withdrawals and transfers
  [notice] ACC-1002 WITHDRAWAL       600.00  -> balance -400.00
  [notice] ACC-1001 TRANSFER_OUT     300.00  -> balance 700.00
  [notice] ACC-1002 TRANSFER_IN      300.00  -> balance -100.00

◆ Rules are enforced by the accounts themselves
  ✖ Rejected: Account ACC-1001: cannot withdraw 1000.00, only 600.00 is available
  ✖ Rejected: Amount must be greater than zero, but was: -50.00

◆ Liskov Substitution: one method, many account types
  Savings ACC-1001 (Alice)     balance    700.00 | available    600.00 | can withdraw 250.00? yes
  Checking ACC-1002 (Bob)      balance   -100.00 | available    400.00 | can withdraw 250.00? yes

◆ Month-end interest (one loop, every account type)
  [notice] ACC-1001 INTEREST           1.75  -> balance 701.75
  [notice] ACC-1003 INTEREST          62.50  -> balance 15062.50
...
```

After that, the program prints an account summary and a full statement for each account.

---

## Try it yourself: extension exercises

Each exercise below should only need **new** classes plus a line or two in `Main`, `GuidedDemo` or `BankCli`. If you find yourself editing an existing class, ask which principle that breaks.

1. **New interest rule** — `BonusInterest`: 1% per year, plus a flat 5.00 bonus if the balance is above 5,000. *(OCP)*
2. **New listener** — `AuditLogListener`: collects every transaction in a list and prints it at the end. *(OCP)*
3. **New account type** — `StudentAccount extends WithdrawableAccount`: no overdraft and a maximum withdrawal of 200 per transaction. Check that `showWithdrawalLimits` and `InterestService` work with it unchanged. *(LSP, OCP)*
4. **New report** — a `CsvStatementExporter` that writes transactions as CSV. *(SRP: formatting stays out of `Account`)*

---

## Design notes

- **Money uses `BigDecimal`**, never `double`, so amounts like `0.1 + 0.2` are exact. Interest is rounded to cents with banker's rounding (`HALF_EVEN`).
- **Data is kept in memory**, so nothing is saved after the program ends. That keeps the example focused. Persistence would only affect the two repository classes.
- **Transfers are safe:** the withdrawal happens first. If it fails, neither account has changed.
