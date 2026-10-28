package com.bank.creational.factory;

import com.bank.account.Account;
import com.bank.account.SavingsAccount;

/** Factory Method implementation for savings accounts. */
public final class SavingsAccountFactory extends AccountFactory {

    @Override
    public String supportedType() {
        return "Savings";
    }

    @Override
    public Account create(AccountSpec spec) {
        require(spec.minimumBalance() != null, "A savings account needs a minimum balance");
        return new SavingsAccount(
                spec.accountNumber(), spec.ownerName(), spec.interestPolicy(), spec.minimumBalance());
    }
}
