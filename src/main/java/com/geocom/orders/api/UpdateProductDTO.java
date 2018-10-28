package com.geocom.orders.api;

import com.geocom.orders.controller.validator.annotation.ValidCurrency;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdateProductDTO implements Serializable {

    @NotNull
    private String name;
    @NotNull
    @Min(1)
    private BigDecimal price;
    @NotNull
    @ValidCurrency
    private String currency;
}
