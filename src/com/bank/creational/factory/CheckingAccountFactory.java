package com.bank.creational.factory;

import com.bank.account.Account;
import com.bank.account.CheckingAccount;

/** Factory Method implementation for checking accounts. */
public final class CheckingAccountFactory extends AccountFactory {

    @Override
    public String supportedType() {
        return "Checking";
    }

    @Override
    public Account create(AccountSpec spec) {
        require(spec.overdraftLimit() != null, "A checking account needs an overdraft limit");
        return new CheckingAccount(
                spec.accountNumber(), spec.ownerName(), spec.interestPolicy(), spec.overdraftLimit());
    }
}
