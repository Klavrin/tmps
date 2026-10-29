package com.bank.interest;

import java.math.BigDecimal;

public interface InterestPolicy {

  BigDecimal monthlyInterestFor(BigDecimal balance);

  String describe();
}
