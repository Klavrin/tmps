package com.bank.creational.factory;

import com.bank.interest.InterestPolicy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Everything a factory needs in order to create one account.
 *
 * <p>Using a small value object keeps the factory method signature stable:
 * adding a new account type does not change the method that every factory
 * implements.
 */
public record AccountSpec(
        String accountNumber,
        String ownerName,
        InterestPolicy interestPolicy,
        BigDecimal minimumBalance,
        BigDecimal overdraftLimit,
        Integer termInMonths) {

    public AccountSpec {
        Objects.requireNonNull(accountNumber, "accountNumber");
        Objects.requireNonNull(ownerName, "ownerName");
        Objects.requireNonNull(interestPolicy, "interestPolicy");
    }
}
