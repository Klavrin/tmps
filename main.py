#!/usr/bin/env python3
"""
Demonstration of a Banking System adhering to the first three SOLID principles:
- S: Single-Responsibility Principle
- O: Open-Closed Principle
- L: Liskov Substitution Principle
"""

from decimal import Decimal

from banking.interest.interest_calculators import (
    FlatRateInterestStrategy,
    TieredInterestStrategy,
)
from banking.models.account import (
    Account,
    CheckingAccount,
    SavingsAccount,
    TermDepositAccount,
    WithdrawableAccount,
)
from banking.models.customer import Customer
from banking.notifications.notification_service import (
    CompositeNotificationChannel,
    EmailNotificationChannel,
    SMSNotificationChannel,
)
from banking.payment_methods.payment_processors import (
    CreditCardPaymentProcessor,
    CryptoPaymentProcessor,
    PayPalPaymentProcessor,
)
from banking.persistence.repository import (
    InMemoryAccountRepository,
    InMemoryCustomerRepository,
    InMemoryTransactionRepository,
)
from banking.services.banking_service import BankingService
from banking.services.statement_service import StatementService


def polymorphic_deposit_all(
    accounts: list[Account], amount: Decimal, service: BankingService
) -> None:
    """
    Demonstrates Liskov Substitution Principle (LSP):
    Every object that inherits from Account can be substituted here seamlessly.
    Whether it's a SavingsAccount, CheckingAccount, or TermDepositAccount,
    the deposit contract is guaranteed.
    """
    print(
        f"\n--- [LSP Demo] Polymorphic deposit of ${amount} into all account types ---"
    )
    for acc in accounts:
        service.deposit(acc.account_id, amount, description="Universal Bonus Deposit")
        print(
            f"-> Successfully deposited to {acc.get_account_type()} ({acc.account_id}). Balance: ${acc.balance:,.2f}"
        )


def polymorphic_withdraw_all(
    accounts: list[WithdrawableAccount], amount: Decimal, service: BankingService
) -> None:
    """
    Demonstrates Liskov Substitution Principle (LSP):
    Every object implementing WithdrawableAccount honors the withdrawal contract.
    Notice that TermDepositAccount is not a WithdrawableAccount, avoiding LSP violation.
    """
    print(
        f"\n--- [LSP Demo] Polymorphic withdrawal of ${amount} from Withdrawable Accounts ---"
    )
    for acc in accounts:
        service.withdraw(acc.account_id, amount, description="Universal ATM Withdrawal")
        print(
            f"-> Successfully withdrew from {acc.get_account_type()} ({acc.account_id}). Remaining balance: ${acc.balance:,.2f}"
        )


def main() -> None:
    print("=" * 70)
    print("  SOLID BANKING SYSTEM ARCHITECTURE DEMO (SRP, OCP, LSP)")
    print("=" * 70)

    account_repo = InMemoryAccountRepository()
    transaction_repo = InMemoryTransactionRepository()
    customer_repo = InMemoryCustomerRepository()

    # Composite notification channel: email + sms
    notifier = CompositeNotificationChannel(
        [EmailNotificationChannel(), SMSNotificationChannel()]
    )

    banking_service = BankingService(
        account_repo=account_repo,
        transaction_repo=transaction_repo,
        customer_repo=customer_repo,
        notification_channel=notifier,
    )
    statement_service = StatementService()

    # Create customer
    alice = Customer(
        customer_id="CUST-101",
        name="Alice Smith",
        email="alice.smith@example.com",
        phone_number="+1-555-0199",
    )
    customer_repo.save(alice)

    # Create distinct account types
    # - Savings account (no overdraft)
    savings = SavingsAccount(
        account_id="ACC-SAV-01",
        customer_id=alice.customer_id,
        initial_balance=Decimal("2000.00"),
        interest_rate=Decimal("0.04"),
    )
    # - Checking account (with $500 overdraft limit)
    checking = CheckingAccount(
        account_id="ACC-CHK-01",
        customer_id=alice.customer_id,
        initial_balance=Decimal("500.00"),
        overdraft_limit=Decimal("300.00"),
    )
    # - Term deposit (locked, not withdrawable on demand)
    term_deposit = TermDepositAccount(
        account_id="ACC-TRM-01",
        customer_id=alice.customer_id,
        initial_balance=Decimal("10000.00"),
        term_months=12,
        interest_rate=Decimal("0.06"),
    )

    account_repo.save(savings)
    account_repo.save(checking)
    account_repo.save(term_deposit)

    # ---------------------------------------------------------
    # 2. DEMONSTRATING SRP (Single Responsibility Principle)
    # ---------------------------------------------------------
    print("\n" + "#" * 70)
    print("PART 1: SINGLE RESPONSIBILITY PRINCIPLE (SRP)")
    print("#" * 70)
    print("Responsibilities are strictly segregated:")
    print(" - Account: Manages internal state and balance calculation.")
    print(" - AccountRepository: Manages persistence (save / query).")
    print(" - NotificationChannel: Formats & sends external alerts (SMS, Email).")
    print(" - StatementService: Formats tabular financial reports.")
    print(" - BankingService: Coordinates workflows between domains.\n")

    banking_service.deposit("ACC-SAV-01", Decimal("500.00"), "Direct Payroll Deposit")
    banking_service.withdraw("ACC-CHK-01", Decimal("200.00"), "Grocery Shopping")

    # ---------------------------------------------------------
    # 3. DEMONSTRATING OCP (Open-Closed Principle)
    # ---------------------------------------------------------
    print("\n" + "#" * 70)
    print("PART 2: OPEN-CLOSED PRINCIPLE (OCP)")
    print("#" * 70)
    print(
        "Adding new payment methods without modifying BankingService or existing processors:\n"
    )

    # Payment via Credit Card
    cc_processor = CreditCardPaymentProcessor()
    cc_result = banking_service.pay_with_processor(
        processor=cc_processor,
        account_id="ACC-CHK-01",
        amount=Decimal("120.00"),
        details={"card_number": "4111 2222 3333 4444"},
    )
    print(f"Credit Card Payment Result: {cc_result.message}")

    # Payment via PayPal (New extension, zero modification to BankingService)
    paypal_processor = PayPalPaymentProcessor()
    paypal_result = banking_service.pay_with_processor(
        processor=paypal_processor,
        account_id="ACC-SAV-01",
        amount=Decimal("85.00"),
        details={"paypal_email": "merchant@store.com"},
    )
    print(f"PayPal Payment Result: {paypal_result.message}")

    # Payment via Crypto (Another extension)
    crypto_processor = CryptoPaymentProcessor()
    crypto_result = banking_service.pay_with_processor(
        processor=crypto_processor,
        account_id="ACC-SAV-01",
        amount=Decimal("50.00"),
        details={"wallet_address": "0x71C845137c37D73F40562D5b7C512D119865B821"},
    )
    print(f"Crypto Payment Result: {crypto_result.message}")

    # OCP in Interest Strategies
    print("\nApplying open interest strategies:")
    flat_strategy = FlatRateInterestStrategy(annual_rate=Decimal("0.05"))
    earned_flat = banking_service.apply_interest("ACC-SAV-01", flat_strategy)
    print(f"Flat rate interest credited: ${earned_flat:,.2f}")

    tiered_strategy = TieredInterestStrategy()
    earned_tiered = banking_service.apply_interest("ACC-TRM-01", tiered_strategy)
    print(f"Tiered interest credited to term deposit: ${earned_tiered:,.2f}")

    # ---------------------------------------------------------
    # 4. DEMONSTRATING LSP (Liskov Substitution Principle)
    # ---------------------------------------------------------
    print("\n" + "#" * 70)
    print("PART 3: LISKOV SUBSTITUTION PRINCIPLE (LSP)")
    print("#" * 70)

    # 4.1 All Account subtypes can substitute Account for deposits
    all_accounts: list[Account] = [savings, checking, term_deposit]
    polymorphic_deposit_all(all_accounts, Decimal("100.00"), banking_service)

    # 4.2 Withdrawable accounts honor withdrawal contract
    withdrawable_accounts: list[WithdrawableAccount] = [savings, checking]
    polymorphic_withdraw_all(withdrawable_accounts, Decimal("50.00"), banking_service)

    # 4.3 CheckingAccount overdraft behavior honors LSP contract:
    # Checking account balance is currently around $230 - withdrawing $300 dips into overdraft without breaking!
    print(
        f"\nChecking Account balance before overdraft withdrawal: ${checking.balance:,.2f}"
    )
    print(
        f"Checking Account available funds (incl. ${checking.overdraft_limit} overdraft): ${checking.get_available_funds():,.2f}"
    )
    banking_service.withdraw("ACC-CHK-01", Decimal("350.00"), "Overdraft Purchase")
    print(
        f"Checking Account balance after overdraft withdrawal: ${checking.balance:,.2f} (Negative balance supported)"
    )

    # ---------------------------------------------------------
    # 5. STATEMENT PRESENTATION (SRP)
    # ---------------------------------------------------------
    print("\n" + "#" * 70)
    print("PART 4: STATEMENT PRESENTATION (SRP Decoupling)")
    print("#" * 70)
    history = transaction_repo.get_by_account_id("ACC-SAV-01")
    statement = statement_service.generate_statement(alice, savings, history)
    print(statement)


if __name__ == "__main__":
    main()
