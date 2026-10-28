package com.bank.creational.prototype;

import com.bank.account.Account;
import com.bank.service.AccountNumberGenerator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Keeps pre-configured accounts and creates new ones by cloning them.
 *
 * <p><b>Creational pattern: Prototype.</b> Some accounts are configured the
 * same way for every customer (for example a student account with a 1% rate
 * and no minimum balance). Instead of repeating that configuration each time,
 * the registry stores a prototype and asks it to copy itself. The copy gets a
 * fresh account number and owner, and starts with a zero balance.
 *
 * <p>Cloning is cheaper and less error-prone than rebuilding a complex object
 * from scratch, and it keeps the configuration in exactly one place.
 */
public final class AccountTemplateRegistry {

    private final Map<String, Account> templates = new LinkedHashMap<>();
    private final AccountNumberGenerator numberGenerator;

    public AccountTemplateRegistry(AccountNumberGenerator numberGenerator) {
        this.numberGenerator = Objects.requireNonNull(numberGenerator, "numberGenerator");
    }

    /** Stores an account to be used as a prototype under {@code key}. */
    public void register(String key, Account template) {
        templates.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(template, "template"));
    }

    /** Clones the prototype registered under {@code key} for a new owner. */
    public Account createFrom(String key, String ownerName) {
        Account template = templates.get(key);
        if (template == null) {
            throw new IllegalArgumentException("No account template registered for '" + key + "'");
        }
        return template.copy(numberGenerator.next(), ownerName);
    }

    /** The keys of every registered prototype. */
    public List<String> keys() {
        return List.copyOf(templates.keySet());
    }
}
