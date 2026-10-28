package com.bank.creational.abstractfactory;

import com.bank.creational.factory.AccountFactory;
import com.bank.creational.factory.SavingsAccountFactory;
import com.bank.creational.singleton.BankConfig;
import com.bank.interest.FixedRateInterest;
import com.bank.interest.InterestPolicy;
import com.bank.notification.LargeTransactionAlert;
import com.bank.notification.TransactionListener;

/**
 * The premium family: a better default rate and alerts for large movements.
 * The alert threshold comes from the {@link BankConfig} singleton, so the
 * whole application agrees on what "large" means.
 */
public final class PremiumBankFactory implements BankFactory {

    @Override
    public String name() {
        return "Premium Bank";
    }

    @Override
    public AccountFactory accountFactory() {
        return new SavingsAccountFactory();
    }

    @Override
    public InterestPolicy defaultInterest() {
        return new FixedRateInterest(5.0);
    }

    @Override
    public TransactionListener notifier() {
        return new LargeTransactionAlert(BankConfig.getInstance().largeTransactionThreshold());
    }
}
