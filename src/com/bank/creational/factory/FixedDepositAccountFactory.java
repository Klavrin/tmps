package com.bank.creational.factory;

import com.bank.account.Account;
import com.bank.account.FixedDepositAccount;

/** Factory Method implementation for fixed-deposit accounts. */
public final class FixedDepositAccountFactory extends AccountFactory {

    @Override
    public String supportedType() {
        return "Fixed Deposit";
    }

    @Override
    public Account create(AccountSpec spec) {
        require(spec.termInMonths() != null, "A fixed deposit needs a term in months");
        return new FixedDepositAccount(
                spec.accountNumber(), spec.ownerName(), spec.interestPolicy(), spec.termInMonths());
    }
}
