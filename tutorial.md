# SOLID Bank: a tutorial

This tutorial explains the whole app in plain language: what it does, how to use each screen, and what every class in the code is for. You don't need to know anything about the project beforehand. Basic Java (classes, interfaces, `if`, loops) is enough.

**Contents**

1. [What is this app?](#1-what-is-this-app)
2. [Running it](#2-running-it)
3. [Your first five minutes: a guided tour](#3-your-first-five-minutes-a-guided-tour)
4. [Every screen and menu, explained](#4-every-screen-and-menu-explained)
5. [The bank's rules](#5-the-banks-rules)
6. [How the code is organised](#6-how-the-code-is-organised)
7. [Every class, one by one](#7-every-class-one-by-one)
8. [Following one action through the code](#8-following-one-action-through-the-code)
9. [How the terminal tricks work](#9-how-the-terminal-tricks-work)
10. [The three SOLID principles in this app](#10-the-three-solid-principles-in-this-app)
11. [Exercises](#11-exercises)
12. [Troubleshooting and FAQ](#12-troubleshooting-and-faq)
13. [Glossary](#13-glossary)

---

## 1. What is this app?

SOLID Bank is a small pretend bank that runs in your terminal. You can:

- see all accounts and their balances on one screen,
- deposit, withdraw and transfer money,
- pay the monthly interest,
- read the full history (statement) of an account,
- open new accounts,
- run a guided demo that walks through the ideas behind the code.

The app has two goals:

1. **Be pleasant to use**: colours, arrow-key menus and clear messages.
2. **Teach good design**: the code follows the first three **SOLID** principles. They're explained in [section 10](#10-the-three-solid-principles-in-this-app).

| Letter | Principle | In one sentence |
|---|---|---|
| **S** | Single Responsibility | Every class has one job. |
| **O** | Open/Closed | You add features by writing *new* classes, not by editing old ones. |
| **L** | Liskov Substitution | A child class can always stand in for its parent class without causing surprises. |

Everything is kept **in memory**. When you quit, all data is gone, and every run starts fresh with the same three sample accounts.

---

## 2. Running it

### What you need

- **Java 17 or newer.** Check with `java -version`.
- Nothing else: no Maven, no Gradle, no libraries.

### Start the app

From the project folder:

```bash
./run.sh
```

The script:

1. deletes the old `out/` folder,
2. compiles every `.java` file in `src/` into `out/`,
3. starts `com.bank.Main`.

### Start only the guided demo

```bash
./run.sh --demo
```

This prints the scripted walkthrough and exits. You don't need to press any keys.

### Without the script

```bash
javac -d out $(find src -name "*.java")
java -cp out com.bank.Main          # the app
java -cp out com.bank.Main --demo   # the demo
```

On Windows (PowerShell):

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out com.bank.Main
```

### Two modes: arrow keys or numbers

The app picks one of two modes by itself when it starts:

| Mode | When | How you choose things |
|---|---|---|
| **Arrow-key mode** | A real terminal on macOS or Linux | Move with `↑`/`↓` and press `Enter` |
| **Numbered mode** | Windows, IDE consoles (IntelliJ, VS Code's output panel), or piped input | Type a number and press `Enter` |

Both modes have the same screens and the same features. Only the way you pick menu items is different. [Section 9](#9-how-the-terminal-tricks-work) explains why.

### Turning colours off

```bash
NO_COLOR=1 ./run.sh
```

This is useful when you copy output into a document, or if your terminal shows strange characters like `[36m`.

---

## 3. Your first five minutes: a guided tour

Start the app with `./run.sh`. Follow these steps, and you'll have seen almost every feature.

### Step 1: look at the dashboard

```
  ╭──────────────────────────────────────────────────────────────────────────╮
  │ ◆ SOLID BANK  S · O · L principles           3 accounts  total 16,200.00 │
  ╰──────────────────────────────────────────────────────────────────────────╯

  NUMBER    TYPE                       OWNER             BALANCE    AVAILABLE
  ACC-1001  Savings                    Alice            1,000.00       900.00
  ACC-1002  Checking                   Bob                200.00       700.00
  ACC-1003  Fixed Deposit (12 months)  Carol           15,000.00       locked

  RECENT ACTIVITY
  ▲ ACC-1001  Deposit           +1,000.00   balance 1,000.00
  ▲ ACC-1002  Deposit             +200.00   balance 200.00
  ▲ ACC-1003  Deposit          +15,000.00   balance 15,000.00  ⚠ large

  What would you like to do?

  ❯ Deposit             put money into any account
    Withdraw            take money out of savings or checking
    Transfer            move money between two accounts
    Month-end interest  pay interest on every account
    Statement           full history of one account
    Open account        savings, checking or fixed deposit
    Guided demo         the scripted SOLID walkthrough
    Quit

  ↑/↓ move · enter select · esc back
```

Notice:

- **Alice** has 1,000.00 but can only take out **900.00**, because a savings account must keep a minimum of 100.
- **Bob** has 200.00 but can take out **700.00**, because his checking account allows a 500 overdraft.
- **Carol**'s money is **locked**: a fixed deposit doesn't allow withdrawals at all.
- Carol's 15,000 deposit is marked **⚠ large** because it's above 10,000.

### Step 2: make a deposit

1. *Deposit* is already highlighted (`❯`). Press `Enter`.
2. Alice is highlighted. Press `Enter`.
3. Type `250` and press `Enter`.

You'll see `✔ Deposited 250.00 into ACC-1001`. Alice now has 1,250.00 and 1,150.00 available.

### Step 3: try to break a rule

1. Press `↓` once to reach *Withdraw*, then `Enter`.
2. Pick Alice and type `5000`.

You'll see a red message:

```
  ✖ Account ACC-1001: cannot withdraw 5000.00, only 1150.00 is available
```

Nothing changed: Alice's balance is still 1,250.00. The **account itself** refused, not the menu. That's an important design point (see [section 5](#5-the-banks-rules)).

### Step 4: go into overdraft

Withdraw `600` from **Bob**. It works: his balance becomes **−400.00**, shown in **red**, and he has 100.00 available.

Also notice that the Withdraw menu doesn't list Carol at all. Fixed deposits can't be withdrawn from, so they're never offered.

### Step 5: pay interest

Choose *Month-end interest*. You'll see `✔ Paid month-end interest on 2 accounts`:

- **Alice** earns 3% per year → 1,250.00 × 3 ÷ 100 ÷ 12 = 3.125 → **3.12** (see "banker's rounding" in [section 5](#5-the-banks-rules)).
- **Bob** earns nothing (no interest, and he's overdrawn anyway).
- **Carol** has 15,000, which is above the 10,000 threshold, so she gets the higher 5% rate: 15,000 × 5 ÷ 100 ÷ 12 = **62.50**.

### Step 6: read a statement

Choose *Statement*, then pick Bob. You'll see every transaction on his account, then the closing balance. Press any key to go back.

### Step 7: open your own account

Choose *Open account* and answer the questions, for example:

| Question | Answer |
|---|---|
| What kind of account? | Savings |
| Who owns the new account? | `Dave` |
| What minimum balance must Dave keep? | `50` |
| Which interest rule? | Fixed rate |
| Yearly interest rate, in %? | `2.5` |
| Opening deposit? | `300` |

Dave now appears as `ACC-1004` with 300.00, of which 250.00 is available.

### Step 8: watch the guided demo, then quit

Choose *Guided demo*. The app steps out of full-screen mode and prints the walkthrough so you can scroll through it. Press any key to come back. Then press `q` (or choose *Quit*) to leave.

---

## 4. Every screen and menu, explained

### 4.1 Keys

| Key | What it does |
|---|---|
| `↑` / `↓` | Move the highlight up or down. It wraps around at the top and bottom. |
| `k` / `j` | Same as `↑` / `↓` (for people used to Vim). |
| `1` … `9` | Jump straight to that menu entry and select it. |
| `Enter` | Select the highlighted entry, or confirm what you typed. |
| `Esc` or `q` | In a menu: go back. On the main menu: quit the app. |
| `Backspace` | Delete the last character you typed. |
| `Esc` or empty `Enter` | When typing an answer: cancel the question. |
| `Ctrl+C` | Quit immediately. The terminal is still restored properly. |

In **numbered mode** you type the number of an entry and press `Enter`. An empty `Enter` goes back.

### 4.2 The dashboard

The dashboard is the main screen. It has four parts.

**The header box**

```
  │ ◆ SOLID BANK  S · O · L principles           3 accounts  total 16,200.00 │
```

On the right: how many accounts exist, and the **total** of all balances added together. An overdrawn account (negative balance) lowers the total.

**The accounts table**

| Column | Meaning |
|---|---|
| NUMBER | The account number, like `ACC-1001`. Numbers are handed out in order. |
| TYPE | Savings, Checking, or Fixed Deposit (with its term in months). |
| OWNER | The owner's name. Long names are shortened with `…`. |
| BALANCE | The money in the account. **Red** if negative. |
| AVAILABLE | How much could be withdrawn right now, in **green**. Fixed deposits show `locked`. |

**Recent activity**

The last **5** transactions, oldest at the top:

```
  ▲ ACC-1001  Deposit           +1,000.00   balance 1,000.00
  ▼ ACC-1002  Withdrawal          -600.00   balance -400.00
```

- `▲` in green with `+` means money came **in**: deposit, transfer in, interest.
- `▼` in red with `-` means money went **out**: withdrawal, transfer out.
- "balance" is the account's balance just after that transaction.
- `⚠ large` marks any single transaction above **10,000.00**.

A transfer appears as **two** lines: "Transfer out" on one account and "Transfer in" on the other.

**The status line**

After each action, one line tells you what happened:

| Looks like | Colour | Meaning |
|---|---|---|
| `✔ Deposited 250.00 into ACC-1001` | green | It worked. |
| `✖ Amount must be greater than zero, but was: -50.00` | red | It was refused. The message says why. |
| `● No account earned interest this month` | yellow | Nothing happened, but nothing went wrong either. |

The status line disappears when you start the next action.

### 4.3 Deposit

*"Put money into any account."*

1. Pick **any** account, including fixed deposits.
2. Type the amount.

Every account type accepts deposits.

### 4.4 Withdraw

*"Take money out of savings or checking."*

1. Pick an account. Only **savings and checking** accounts are listed, and each shows how much is **available**.
2. Type the amount.

The withdrawal succeeds if the amount is **not more than** what's available. Otherwise you get a red message and nothing changes.

### 4.5 Transfer

*"Move money between two accounts."*

1. **From**: pick a savings or checking account. A fixed deposit can't be the source, because money can't leave it.
2. **To**: pick any *other* account. The source account isn't listed, so you can't send money to the same account. A fixed deposit **can** receive a transfer.
3. Type the amount.

Behind the scenes, the money is first taken out of the source and then added to the target. If the source doesn't have enough, the whole transfer stops before anything changes, so money is never lost halfway.

### 4.6 Month-end interest

*"Pay interest on every account."*

There's no question to answer. Every account calculates one month of interest using its own rule, and any account that earned more than zero gets paid. Each payment shows up in Recent Activity as "Interest". You can choose this as often as you like: each time counts as another month.

### 4.7 Statement

*"Full history of one account."*

Pick any account to see:

```
  STATEMENT  Checking ACC-1002 (Bob)
  interest   no interest
  ──────────────────────────────────────────────────────────────────────────
  TYPE                 AMOUNT      BALANCE   DESCRIPTION
  Deposit             +200.00       200.00   Cash deposit
  Withdrawal          -600.00      -400.00   Cash withdrawal
  ──────────────────────────────────────────────────────────────────────────
  Closing                          -400.00
```

- The **interest** line describes the account's interest rule.
- The screen shows the last **12** transactions. Older ones are counted in a note: `… 3 earlier transactions not shown`.
- Possible descriptions: `Cash deposit`, `Cash withdrawal`, `Transfer to ACC-…`, `Transfer from ACC-…`, `Monthly interest (…)`.
- **Closing** is the balance right now.

Press any key to go back.

### 4.8 Open account

*"Savings, checking or fixed deposit."*

The app asks a few questions in this order:

1. **What kind of account?**
   - *Savings*: keeps a minimum balance, and you can withdraw the rest.
   - *Checking*: an everyday account that can go into overdraft.
   - *Fixed deposit*: deposits only, and the money is locked for a term.
2. **Who owns it?** Any name.
3. **One question that depends on the type:**
   - Savings: *What minimum balance must they keep?*
   - Checking: *How far may they go into overdraft?*
   - Fixed deposit: *How many months is the term?*
4. **Which interest rule?**
   - *No interest*: earns nothing.
   - *Fixed rate*: one yearly rate, e.g. `3` for 3% per year.
   - *Tiered rate*: asks for a threshold, a lower rate and a higher rate. Below the threshold the whole balance earns the lower rate. At or above it, the whole balance earns the higher rate.
5. **Opening deposit?** Type an amount, or press `Esc`/`Enter` to skip.

If an answer breaks a rule (a negative minimum balance, a term of 0 months, a negative rate), the account isn't created and a red message explains why. Cancelling any question before step 5 cancels the whole thing.

### 4.9 Guided demo

The original scripted walkthrough. It runs on a **separate, fresh bank**, so it doesn't touch your accounts in the app. It prints:

1. opening deposits, with notices and one large-transaction alert,
2. a withdrawal into overdraft and a transfer,
3. two operations the accounts refuse,
4. the Liskov example: one method handling savings and checking accounts the same way,
5. month-end interest,
6. an account summary,
7. a full statement for each account.

The output is printed on your normal terminal screen, so you can scroll up and read it. Press any key to return to the app.

### 4.10 Quit

Leaves the app and puts your terminal back exactly as it was. `Esc` or `q` on the main menu does the same.

### 4.11 Typing amounts

The rules for any question that asks for an amount:

| You type | Result |
|---|---|
| `250` | 250.00 |
| `99.95` | 99.95 |
| `1,500` | 1,500.00 (commas are ignored) |
| `10.555` | error: *Use at most two decimal places* |
| `abc` | error: *"abc" is not an amount* |
| `-50` or `0` | accepted by the prompt, but then **refused by the account**: *Amount must be greater than zero* |

When there's an error, the question is simply asked again with the error shown in red.

Percentages accept numbers like `3`, `2.5` or `3%`. The term in months must be a whole number like `12`.

---

## 5. The bank's rules

All of these rules live in the **account and interest classes**, not in the menus. That's why the same rules apply in the app, in the demo, and in any future program that uses these classes.

### Accounts

| Rule | Where it's enforced |
|---|---|
| Every amount must be greater than zero. | `Account.requirePositive` |
| Every account accepts deposits. | `Account.deposit` |
| **Savings:** available = balance − minimum balance (never below 0). | `SavingsAccount.getAvailableFunds` |
| **Checking:** available = balance + overdraft limit (never below 0). | `CheckingAccount.getAvailableFunds` |
| **Fixed deposit:** no withdrawals, ever. | There's no `withdraw` method on it at all. |
| A withdrawal works only if amount ≤ available. Otherwise nothing changes. | `WithdrawableAccount.withdraw` |
| Minimum balance and overdraft limit can't be negative. | The account constructors |
| A fixed deposit's term must be at least 1 month. | `FixedDepositAccount` constructor |
| You can't transfer to the same account. | `TransactionService.transfer` |

### Interest

Monthly interest = balance × yearly rate ÷ 100 ÷ 12.

| Rule | Example |
|---|---|
| **No interest** always pays 0. | Bob's checking account |
| **Fixed rate** pays the same rate on any balance. | 3% on 1,200.00 → 3.00 per month |
| **Tiered rate** uses the higher rate on the *whole* balance once it reaches the threshold. | 4%/5% with threshold 10,000: on 15,000 → 62.50 |
| A balance of 0 or less earns nothing. | An overdrawn account gets no interest |
| Rates can't be negative. | `FixedRateInterest` constructor |
| Results are rounded to cents with **banker's rounding**. | 3.125 → 3.12, but 3.135 → 3.14 |

**Banker's rounding** (`HALF_EVEN`): when a number is exactly halfway, round to the nearest **even** digit. Normal rounding always rounds .5 up, which over millions of transactions slightly favours one side. Banker's rounding evens that out.

### Money is exact

All money uses Java's `BigDecimal`, never `double`. With `double`, `0.1 + 0.2` equals `0.30000000000000004`. With `BigDecimal` it's exactly `0.3`. For money, that matters.

---

## 6. How the code is organised

```
src/com/bank/
├── Main.java              starts everything
├── Bank.java              builds the bank's parts and connects them
├── GuidedDemo.java        the scripted walkthrough
│
├── account/               the accounts and their rules
├── interest/              the interest rules
├── transaction/           what a transaction looks like
├── repository/            where accounts and transactions are stored
├── service/               operations: move money, pay interest, make numbers
├── notification/          reactions to transactions (notices, alerts)
├── report/                plain-text statements (used by the demo)
├── exception/             the errors the bank can raise
│
├── cli/                   the interactive bank app (screens and menus)
└── console/               general terminal tools (keys, colours, menus)
```

Think of it as **layers**. Each layer only uses the layers below it:

```
   ┌──────────────────────────────────────────────┐
   │  Main, GuidedDemo                            │   start-up
   ├──────────────────────────────────────────────┤
   │  cli/        BankCli, Screens, ActivityFeed  │   the app you see
   ├──────────────────────────────────────────────┤
   │  console/    Terminal, Ui, Ansi              │   terminal toolkit (knows nothing about banks)
   ├──────────────────────────────────────────────┤
   │  service/, notification/, report/            │   operations and reactions
   ├──────────────────────────────────────────────┤
   │  account/, interest/, transaction/,          │   the core: data and rules
   │  repository/, exception/                     │
   └──────────────────────────────────────────────┘
```

The core bank code (`account/`, `interest/`, `service/` …) **never prints anything and never reads the keyboard**. You could put a website or a mobile app on top of it without changing a single line of it.

---

## 7. Every class, one by one

### 7.1 Start-up

#### `Main`

The program's entry point (`public static void main`).

- If you passed `--demo`, it runs `GuidedDemo` and stops.
- Otherwise it:
  1. builds a `Bank`,
  2. creates an `ActivityFeed` (threshold 10,000) and registers it as a listener,
  3. opens the three sample accounts and makes their first deposits,
  4. opens the `Terminal` and runs `BankCli`.

The `try (Terminal terminal = new Terminal()) { … }` block guarantees the terminal is restored when the app ends, even if something goes wrong.

The sample accounts:

| Number | Owner | Type | Interest | Setting | First deposit |
|---|---|---|---|---|---|
| ACC-1001 | Alice | Savings | Fixed 3% | minimum balance 100 | 1,000 |
| ACC-1002 | Bob | Checking | None | overdraft 500 | 200 |
| ACC-1003 | Carol | Fixed deposit | Tiered 4% / 5% above 10,000 | 12 months | 15,000 |

#### `Bank`

A `record` that holds the five parts every bank needs and connects them:

- `AccountRepository`, `TransactionRepository`, `AccountNumberGenerator`, `TransactionService`, `InterestService`.

`Bank.create()` builds them in the right order, because some parts need others: `TransactionService` needs the `TransactionRepository`, for example. Both the app and the demo call `Bank.create()`, so the setup is written only once. A class like this is often called a **composition root**.

#### `GuidedDemo`

The scripted walkthrough (described in [4.9](#49-guided-demo)). It builds its *own* `Bank`, registers `ConsoleNotifier` and `LargeTransactionAlert` as listeners, and runs a fixed list of operations. It contains:

- `showWithdrawalLimits(List<WithdrawableAccount>, …)`, the textbook Liskov example: one loop that works for any withdrawable account, with no `instanceof`.
- A commented-out line, `// bank.transactionService().withdraw(carol, …)`. If you uncomment it, the code **won't compile**, because a fixed deposit isn't a `WithdrawableAccount`.

### 7.2 Accounts (`account/`)

The class family:

```
                 Account                     deposit, balance, interest
                /       \
WithdrawableAccount    FixedDepositAccount   + withdraw, canWithdraw, getAvailableFunds
      /        \
SavingsAccount  CheckingAccount
```

#### `Account` (abstract)

The parent of **every** account. It stores the number, owner, interest rule and balance (starting at 0). It offers:

- `deposit(amount)`: checks that the amount is positive, then adds it. It's `final`, so no child class can change how deposits work.
- `calculateMonthlyInterest()`: asks its `InterestPolicy` how much this month's interest is. It doesn't add the money; that's `TransactionService`'s job.
- `getAccountType()`: abstract. Each child returns a name like "Savings".
- `reduceBalance(amount)`: `protected`, so only account classes can take money out.
- `requirePositive(amount)`: throws `InvalidAmountException` for null, zero or negative amounts.
- `toString()`: gives e.g. `Savings ACC-1001 (Alice)`.

What `Account` deliberately **doesn't** have is a `withdraw` method, because not every account can withdraw.

#### `WithdrawableAccount` (abstract)

An account you can take money out of. It writes the withdrawal rules **once**, as `final` methods:

- `canWithdraw(amount)`: true if the amount is positive and not more than what's available.
- `withdraw(amount)`: checks the amount, then either reduces the balance or throws `InsufficientFundsException`. If it throws, the balance is untouched.
- `getAvailableFunds()`: abstract. This is the **only** thing child classes decide.

#### `SavingsAccount`

Available = balance − minimum balance, never below 0. The minimum balance can't be negative.

#### `CheckingAccount`

Available = balance + overdraft limit, never below 0. The overdraft limit can't be negative. The balance may go below zero, but never past −limit.

#### `FixedDepositAccount`

Extends `Account` directly, **not** `WithdrawableAccount`, so it has no way to withdraw. The term must be at least 1 month. Its type is shown as e.g. `Fixed Deposit (12 months)`.

### 7.3 Interest rules (`interest/`)

#### `InterestPolicy` (interface)

Two methods:

- `monthlyInterestFor(balance)`: how much interest this balance earns in one month.
- `describe()`: a short text like "3% per year", shown on statements.

Every account gets one of these when it's created. The account never needs to know *which* rule it has.

#### `NoInterest`

Always returns 0. Description: "no interest".

#### `FixedRateInterest`

balance × rate ÷ 1200, rounded to cents with banker's rounding. Returns 0 if the balance is 0 or negative. A negative rate is refused. Description: e.g. "3% per year".

#### `TieredInterest`

Holds two `FixedRateInterest` objects, a lower and an upper tier. If the balance is at or above the threshold it uses the upper one, otherwise the lower one. It **reuses** `FixedRateInterest` instead of repeating its maths. Description: e.g. "4% per year below 10000, 5% per year above".

### 7.4 Transactions (`transaction/`)

#### `TransactionType` (enum)

The five kinds of money movement: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`, `INTEREST`.

#### `Transaction` (record)

An unchangeable note of one movement: type, account number, amount, balance after, description and time. Because it's a `record`, Java writes the constructor, getters, `equals`, `hashCode` and `toString` for you.

### 7.5 Storage (`repository/`)

#### `AccountRepository`

Stores accounts by number in a `LinkedHashMap`, which keeps them in the order they were added. `save`, `findByNumber`, `findAll`. If you wanted a real database later, only this class would change.

#### `TransactionRepository`

Stores every transaction in a list. `save`, and `findByAccountNumber`, which the statements use. Accounts don't keep their own history; storing it is this class's job.

### 7.6 Services (`service/`)

#### `TransactionService`

The class that **moves money**. Every operation goes through it:

- `deposit(account, amount)`: any `Account`.
- `withdraw(account, amount)`: only a `WithdrawableAccount`. The parameter type itself stops fixed deposits.
- `transfer(from, to, amount)`: `from` must be withdrawable, `to` can be any account. It withdraws first, then deposits, so a failed withdrawal leaves both accounts untouched.
- `payInterest(account, amount)`: deposits interest and records it with a description.
- `addListener(listener)`: registers something that wants to hear about every transaction.

After each successful operation it creates a `Transaction`, saves it in the repository, and tells **every listener** about it.

#### `InterestService`

`applyMonthlyInterest()` loops over all accounts, asks each one for its monthly interest, and pays it through `TransactionService` if it's above zero. There's **no** `if (account is savings…)` anywhere, because each account already knows its own rule.

#### `AccountNumberGenerator`

Hands out `ACC-1001`, `ACC-1002`, `ACC-1003`, … one after another.

### 7.7 Reactions (`notification/`)

#### `TransactionListener` (interface)

One method: `onTransaction(transaction)`. Anything that wants to react to transactions implements it.

#### `ConsoleNotifier`

Prints a coloured `[notice]` line for every transaction. Used by the demo.

#### `LargeTransactionAlert`

Prints a yellow `[ALERT]` line when a transaction is above its threshold. Used by the demo.

### 7.8 Reports (`report/`)

#### `StatementPrinter`

Prints the plain-text account summary and statements that the **demo** shows. The interactive app uses its own fancier version, `Screens`. Two classes that format output differently, and neither touches the accounts: that's the point of keeping formatting separate.

### 7.9 Errors (`exception/`)

#### `InsufficientFundsException`

Thrown when a withdrawal asks for more than is available. Message: *Account ACC-1001: cannot withdraw 5000.00, only 1150.00 is available*.

#### `InvalidAmountException`

Thrown for null, zero or negative amounts. Message: *Amount must be greater than zero, but was: -50.00*.

The app catches these and shows their message in red. It never repeats the rule itself.

### 7.10 The interactive app (`cli/`)

#### `BankCli`

The app's "brain". It holds the main menu as a list of `MenuItem(label, detail, action)` entries, and its `run()` method loops:

1. draw the dashboard and ask which menu item you want,
2. clear the old status message,
3. run that item's action,
4. repeat until you choose *Quit* or press `Esc`.

Each action (`deposit`, `withdraw`, `transfer`, `payInterest`, `showStatement`, `openAccount`, `runGuidedDemo`) asks its questions and then calls the bank. Useful helpers:

- `attempt(operation, successMessage)`: runs a bank operation. If it works, it sets a green ✔ message; if the bank throws, a red ✖ message with the bank's own explanation.
- `pickAccount(title, accounts, detail)`: shows a menu of accounts and returns the chosen one, or `null` if you went back. The `detail` function decides what's shown next to each account: "balance …" or "available …".
- `askMoney`, `askPercent`, `askWholeNumber`: ask a question and keep asking, with an error shown, until the answer is valid or you cancel.
- `askInterestPolicy()`: the interest-rule menu. Adding a new rule means one more option here plus a new `InterestPolicy` class.
- `withdrawableAccounts()`: filters the account list down to the withdrawable ones, for the Withdraw and Transfer menus.
- `dashboard()`: builds the dashboard text from `Screens`, adding the status line if there is one.

`BankCli` never does maths on balances and never checks business rules. It asks, calls, and reports.

#### `Screens`

Turns data into coloured text: `header`, `accountTable`, `activity`, `statement`. It only **returns strings**. It doesn't print or read anything, which makes it easy to change the look without touching anything else. It also formats money with thousands separators (`1,250.00`).

#### `ActivityFeed`

A `TransactionListener` that remembers every transaction and marks the ones above 10,000 as large. The dashboard shows `latest(5)`. `BankCli` uses `size()` before and after paying interest to count how many accounts were paid.

### 7.11 The terminal toolkit (`console/`)

These three classes know nothing about banks. You could copy them into any other Java project.

#### `Ansi`

Colour helpers: `bold`, `dim`, `red`, `green`, `yellow`, `cyan`, …, plus `style(codes, text)` for combinations (`"1;36"` = bold cyan). It also has text helpers that ignore the invisible colour codes when measuring: `visibleLength`, `padLeft`, `padRight`, and `fit`, which shortens text with `…`. If `NO_COLOR` is set, every colour helper returns the plain text.

#### `Terminal`

Low-level terminal access:

- switches the keyboard to "raw" mode so single keys can be read (see [section 9](#9-how-the-terminal-tricks-work)),
- `readKey()` turns bytes into keys: `UP`, `DOWN`, `ENTER`, `ESCAPE`, `BACKSPACE`, a normal `CHARACTER`, or `END_OF_INPUT`,
- `readLine()` reads a whole line, for numbered mode,
- `draw(frame)` replaces the whole screen with new content,
- `enterFullScreen()` / `leaveFullScreen()` switch between the app's own screen and your normal one,
- `showCursor(…)`,
- `close()` and a shutdown hook put everything back when the app ends.

`isInteractive()` tells the rest of the app which mode it's in.

#### `Ui`

The question-asking layer, built on `Terminal`:

- `choose(screen, title, options)`: a menu. In arrow-key mode it redraws with the highlight moving. In numbered mode it prints numbers and reads a line. It returns the chosen index, or **−1** for "went back".
- `ask(screen, question, hint, error)`: asks for text. Returns the answer, or `null` if cancelled.
- `pause(screen)`: shows a screen and waits for a key.
- `pauseAfterOutput(message)`: waits for a key after normal printing (used after the demo).
- `Option(label, detail)`: one menu entry.

Because of `Ui`, `BankCli` doesn't care which mode it's in. It just calls `choose` or `ask`.

---

## 8. Following one action through the code

Here's what happens when you **withdraw 600 from Bob**, step by step:

```
You press ↓, Enter            Ui.choose  → returns 1 ("Withdraw")
                              BankCli.run → calls BankCli.withdraw()

BankCli.withdraw()
  withdrawableAccounts()      → [Alice, Bob]   (Carol is filtered out)
  pickAccount(...)            → Ui.choose → you pick Bob
  askMoney(...)               → Ui.ask → you type "600" → 600.00
  attempt(...)
    └─ TransactionService.withdraw(bob, 600.00)
         └─ WithdrawableAccount.withdraw(600.00)
              ├─ requirePositive(600.00)            ok
              ├─ canWithdraw(600.00)
              │    └─ CheckingAccount.getAvailableFunds()  → 200 + 500 = 700   ok
              └─ reduceBalance(600.00)              balance: 200 → -400
         └─ record(WITHDRAWAL, ...)
              ├─ new Transaction(...)
              ├─ TransactionRepository.save(...)
              └─ every listener.onTransaction(...)
                   └─ ActivityFeed remembers it
    status = "✔ Withdrew 600.00 from ACC-1002"

Back in BankCli.run            dashboard() → Screens draws the table (Bob in red),
                              the activity feed (▼ Withdrawal) and the status line
```

And if you had typed `5000`, `canWithdraw` would be false, `withdraw` would throw `InsufficientFundsException`, and `attempt` would catch it and show the message in red. The balance, the repository and the feed would all be unchanged, because the exception happens *before* anything is changed or recorded.

---

## 9. How the terminal tricks work

You don't need this section to use the app. It's for the curious.

### Why arrow keys need a "raw" terminal

Normally a terminal waits until you press `Enter` before sending what you typed to the program, and it shows (echoes) every key on screen. That's fine for typing commands, but useless for menus: the program would never see a single `↑`.

The app runs the system command:

```
stty -icanon -echo min 1 time 0
```

- `-icanon` sends each key immediately, without waiting for Enter,
- `-echo` stops showing keys automatically (the app draws what it wants instead),
- `min 1 time 0` means "give me each byte as soon as one arrives".

Before that, it saves your original settings with `stty -g`, and it restores them when it ends. A **shutdown hook** makes sure this also happens if you press `Ctrl+C`.

`stty` only exists on macOS and Linux, and it only works when the program is attached to a real terminal. In any other case the command fails, and the app quietly switches to numbered mode.

### What an arrow key really sends

An arrow key isn't one byte but three: `ESC [ A` for up and `ESC [ B` for down. `Esc` on its own sends just `ESC`. So when the app reads an `ESC`, it waits a few milliseconds:

- if `[` and a letter follow, it was an arrow key,
- if nothing follows, it was the `Esc` key.

### Colours

Colours are special invisible text called **ANSI escape codes**. For example, `ESC[32m` means "green from here", and `ESC[0m` means "back to normal". `Ansi.green("hi")` just wraps the text in those codes.

Because those codes take up characters but no screen space, the `Ansi.padLeft`/`padRight` helpers strip them before measuring, so the columns stay lined up.

### Drawing without flicker

Each time something changes, the app builds the **whole screen** as one string (a "frame") and sends it at once:

- `ESC[H` moves the cursor to the top-left,
- each line ends with `ESC[K` (clear the rest of the line),
- the frame ends with `ESC[J` (clear everything below).

It draws *over* the old screen instead of clearing it first, which is why nothing flickers.

### The full-screen buffer

`ESC[?1049h` switches to a separate "alternate" screen, the same thing editors like `vim` and tools like `less` use. When you quit, `ESC[?1049l` switches back, and your old terminal content reappears untouched. The guided demo switches back to the normal screen temporarily, so its long output stays in your scrollback.

---

## 10. The three SOLID principles in this app

### S: Single Responsibility

> *A class should have one, and only one, reason to change.*

| If you want to change… | …you only touch |
|---|---|
| the rules of savings accounts | `SavingsAccount` |
| where accounts are stored (e.g. a database) | `AccountRepository` |
| how the dashboard looks | `Screens` |
| how the demo's statements look | `StatementPrinter` |
| the account number format | `AccountNumberGenerator` |
| how money moves | `TransactionService` |
| the menu wording or order | `BankCli` |
| how keys are read | `Terminal` |
| the colours | `Ansi` |

The common mistake is one huge `Account` class that holds the balance, stores the history, prints statements and sends emails. Here those are four separate classes. You can check it: `Account` never calls `System.out.println`.

### O: Open/Closed

> *Open for extension, closed for modification.*

There are two "plug-in points", both interfaces:

1. **`InterestPolicy`**: hand each account any rule you like. `InterestService` never changes.
2. **`TransactionListener`**: register any reaction. `TransactionService` never changes.

The app itself proves this. Its live activity panel, `ActivityFeed`, was added *later*, as a new listener, **without editing `TransactionService` at all**. The demo plugs in `ConsoleNotifier` and `LargeTransactionAlert` in exactly the same way.

New account types are also an extension: a new `StudentAccount extends WithdrawableAccount` would work with `InterestService`, `TransactionService`, the dashboard and the Withdraw menu straight away.

### L: Liskov Substitution

> *A child class must work anywhere its parent is expected, without surprises.*

**The trap:** put `withdraw()` on `Account`. Then `FixedDepositAccount` would have to override it with `throw new UnsupportedOperationException()`, and any loop over accounts that calls `withdraw` would crash on a fixed deposit.

**What this app does instead:**

1. **Parents promise only what all children can do.** `Account` promises deposits. Only `WithdrawableAccount` promises withdrawals, and `FixedDepositAccount` doesn't extend it.
2. **Children can't change the rules.** `deposit`, `withdraw` and `canWithdraw` are `final`. Children only answer "how much is available?".
3. **The compiler checks it.** `TransactionService.withdraw` takes a `WithdrawableAccount`, so passing a fixed deposit is a compile error, not a crash.
4. **You can see it in the app.** The Withdraw and Transfer-from menus list only withdrawable accounts. The dashboard says `locked` for fixed deposits instead of inventing an "available" number. The detail column in the Withdraw menu calls `getAvailableFunds()` on savings and checking accounts alike, and both answer correctly.

---

## 11. Exercises

Each exercise should need only **new** classes plus a line or two where things are wired up (`Main`, `GuidedDemo` or `BankCli`). If you find yourself editing an existing class's logic, ask which principle that breaks.

1. **New interest rule:** `BonusInterest` pays 1% per year, plus a flat 5.00 bonus when the balance is above 5,000. Add it as a fourth choice in `BankCli.askInterestPolicy`. *(OCP)*
2. **New listener:** `AuditLogListener` collects every transaction and prints them all when the app quits. *(OCP)*
3. **New account type:** `StudentAccount extends WithdrawableAccount`, with no overdraft and at most 200 per withdrawal. Check that it shows up in the Withdraw menu and earns interest without changing `InterestService`. *(LSP, OCP)*
4. **New report:** `CsvStatementExporter` writes an account's transactions as CSV. *(SRP)*
5. **New menu item:** a "Close account" option. Which classes need to change, and why? (Hint: does `AccountRepository` have a way to remove accounts yet?)
6. **Theme:** change the app's accent colour from cyan to magenta. How many files did you touch?

---

## 12. Troubleshooting and FAQ

**I see numbers instead of an arrow-key menu.**
You're in numbered mode: you're probably running inside an IDE console or on Windows. Run `./run.sh` in a real macOS/Linux terminal (Terminal, iTerm2, a Linux terminal emulator) to get arrow keys.

**I see weird text like `[36m` or `[1;31m`.**
Your console doesn't understand colour codes. Run with `NO_COLOR=1`.

**Box lines or symbols (`╭ ◆ ❯ ▲`) look broken.**
Your terminal isn't using UTF-8 or a font that has these characters. Most modern terminals are fine. On Windows, try Windows Terminal.

**My terminal stopped showing what I type after the app crashed.**
That shouldn't happen, because the app restores the terminal even on `Ctrl+C`. If it ever does (e.g. the process was killed with `kill -9`), type `reset` or `stty sane` and press Enter, even if you can't see the letters.

**Where did my accounts go?**
Nothing is saved; data lives in memory only. Every start gives you the three sample accounts again.

**Why doesn't the demo change my accounts?**
The demo builds its own fresh `Bank`, so it's completely separate from the app's bank.

**Why can't I withdraw from Carol's account?**
It's a fixed deposit: money is locked for the term. That's a rule of the account type, not a bug, and it's the Liskov example of the whole project.

**Why did Alice get 3.12 instead of 3.13?**
Banker's rounding: 3.125 is exactly halfway, so it rounds to the even digit, 2. See [section 5](#5-the-banks-rules).

---

## 13. Glossary

| Term | Meaning |
|---|---|
| **Abstract class** | A class you can't create directly; it's a template for child classes (e.g. `Account`). |
| **ANSI escape code** | Invisible text that tells a terminal to change colour, move the cursor, etc. |
| **Available funds** | How much can be withdrawn right now, which isn't always the same as the balance. |
| **Balance** | How much money is in the account. Can be negative for checking accounts. |
| **Banker's rounding** | Rounding exact halves to the nearest even digit (`HALF_EVEN`). |
| **`BigDecimal`** | A Java number type that stores decimals exactly, which is what money needs. |
| **Composition root** | The one place where all the parts are created and connected (`Bank.create`). |
| **Enum** | A fixed list of named values (e.g. `TransactionType`). |
| **`final` method** | A method child classes can't override. |
| **Fixed deposit** | An account where money is locked for a term; deposits only. |
| **Interface** | A list of methods a class promises to have (e.g. `InterestPolicy`). |
| **Listener** | An object that gets told when something happens (`TransactionListener`). |
| **Overdraft** | Going below zero on a checking account, up to an agreed limit. |
| **Raw mode** | Terminal mode where each key is sent immediately and not echoed. |
| **Record** | A short Java class for plain data; Java writes the boilerplate for you. |
| **Repository** | A class whose only job is storing and finding things. |
| **Service** | A class that carries out operations using other classes (e.g. `TransactionService`). |
| **Shutdown hook** | Code Java runs when the program is ending, even after `Ctrl+C`. |
| **SOLID** | Five design principles; this project covers the first three (S, O, L). |
| **Statement** | The list of all transactions on one account. |
| **Tiered interest** | A lower rate below a threshold, a higher rate at or above it. |
| **Transaction** | A record of one money movement. |
