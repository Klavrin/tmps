package com.bank.creational.singleton;

import java.math.BigDecimal;

/**
 * Only one instance of BankConfig
 */
public final class BankConfig {

  private static volatile BankConfig instance;

  private final String currencyCode;
  private final BigDecimal largeTransactionThreshold;
  private final String accountNumberPrefix;
  private final int firstAccountNumber;
  private final BigDecimal defaultSavingsRate;

  private BankConfig() {
    this.currencyCode = "USD";
    this.largeTransactionThreshold = new BigDecimal("10000");
    this.accountNumberPrefix = "ACC-";
    this.firstAccountNumber = 1001;
    this.defaultSavingsRate = new BigDecimal("3.0");
  }

  /**
   * Returns the one and only configuration object. The first call creates
   * it; every later call returns the same instance.
   */
  public static BankConfig getInstance() {
    BankConfig result = instance;
    if (result == null) {
      synchronized (BankConfig.class) {
        result = instance;
        if (result == null) {
          result = new BankConfig();
          instance = result;
        }
      }
    }
    return result;
  }

  public String currencyCode() {
    return currencyCode;
  }

  public BigDecimal largeTransactionThreshold() {
    return largeTransactionThreshold;
  }

  public String accountNumberPrefix() {
    return accountNumberPrefix;
  }

  public int firstAccountNumber() {
    return firstAccountNumber;
  }

  public BigDecimal defaultSavingsRate() {
    return defaultSavingsRate;
  }
}
