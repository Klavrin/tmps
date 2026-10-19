package com.bank.cli;

import com.bank.notification.TransactionListener;
import com.bank.transaction.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Remembers transactions so the dashboard can show the latest ones, and
 * flags those above a threshold.
 *
 * <p><b>Open/Closed:</b> this is just another {@link TransactionListener}.
 * The interactive app plugs it into {@code TransactionService} the same way
 * the guided demo plugs in {@code ConsoleNotifier}; the service never changed.
 */
public class ActivityFeed implements TransactionListener {

    public record Entry(Transaction transaction, boolean large) {
    }

    private final BigDecimal largeThreshold;
    private final List<Entry> entries = new ArrayList<>();

    public ActivityFeed(BigDecimal largeThreshold) {
        this.largeThreshold = largeThreshold;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        entries.add(new Entry(transaction, transaction.amount().compareTo(largeThreshold) > 0));
    }

    /** The most recent {@code count} entries, oldest first. */
    public List<Entry> latest(int count) {
        return List.copyOf(entries.subList(Math.max(0, entries.size() - count), entries.size()));
    }

    public int size() {
        return entries.size();
    }
}
