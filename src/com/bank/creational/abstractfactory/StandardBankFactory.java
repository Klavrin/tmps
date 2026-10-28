package com.bank.creational.abstractfactory;

import com.bank.creational.factory.AccountFactory;
import com.bank.creational.factory.SavingsAccountFactory;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.InterestPolicy;
import com.bank.notification.ConsoleNotifier;
import com.bank.notification.TransactionListener;

/**
 * The standard family: ordinary savings accounts, a 3% default rate and a
 * simple console notice for every transaction.
 */
public final class StandardBankFactory implements BankFactory {

    @Override
    public String name() {
        return "Standard Bank";
    }

    @Override
    public AccountFactory accountFactory() {
        return new SavingsAccountFactory();
    }

    @Override
    public InterestPolicy defaultInterest() {
        return new FixedRateInterest(3.0);
    }

    @Override
    public TransactionListener notifier() {
        return new ConsoleNotifier();
    }
}
