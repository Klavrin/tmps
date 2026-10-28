package com.bank.creational.abstractfactory;

import com.bank.creational.factory.AccountFactory;
import com.bank.interest.InterestPolicy;
import com.bank.notification.TransactionListener;

/**
 * Creates a whole family of related banking objects.
 *
 * <p><b>Creational pattern: Abstract Factory.</b> A "standard" bank and a
 * "premium" bank need pieces that belong together: the right account factory,
 * the right default interest rule and the right notifier. If the client
 * created those pieces itself, it could accidentally mix a premium interest
 * rate with a standard notifier. By asking one factory for the whole family,
 * the pieces always match.
 *
 * <p>The client only knows this interface, never the concrete classes.
 */
public interface BankFactory {
  /** Human-readable name of this bank tier. */
  String name();

  /** Factory used to open this tier's accounts. */
  AccountFactory accountFactory();

  /** The default interest rule for savings accounts in this tier. */
  InterestPolicy defaultInterest();

  /** How this tier notifies customers about transactions. */
  TransactionListener notifier();
}
