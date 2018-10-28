package com.geocom.orders.service;

import java.math.BigDecimal;

public interface FixerService {

    BigDecimal convert(String baseCurrency, String destCurrency, BigDecimal value);
}
