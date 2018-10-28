package com.geocom.orders.api;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RateDTO implements Serializable {

    String currency;
    BigDecimal value;
}
