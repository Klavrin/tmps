package com.bank.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * An immutable record of one money movement on one account.
 *
 * <p><b>Single Responsibility:</b> this is pure data - it just describes
 * what happened. A Java {@code record} gives us the constructor, getters,
 * equals/hashCode and toString for free.
 */
public record Transaction(
        TransactionType type,
        String accountNumber,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime timestamp) {
}
