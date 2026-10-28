package com.bank.creational.factory;

import com.bank.account.Account;

/**
 * Abstract creator for the Factory Method pattern.
 *
 * <p><b>Creational pattern: Factory Method.</b> The base class defines the
 * operation ({@link #create(AccountSpec)}) but deliberately does not know which
 * concrete {@link Account} to instantiate. Each subclass answers that question.
 *
 * <p>The rest of the program can depend on this class alone. Adding a new
 * account type means adding one new factory subclass, not editing the client.
 */
public abstract class AccountFactory {

    /** A short name for the account type this factory produces. */
    public abstract String supportedType();

    /** Creates one account from the supplied specification. */
    public abstract Account create(AccountSpec spec);

    /** Shared validation helper for subclasses. */
    protected static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
