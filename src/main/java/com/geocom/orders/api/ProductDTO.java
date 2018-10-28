package com.geocom.orders.api;

import com.geocom.orders.controller.validator.annotation.ValidCurrency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO implements Serializable {

    @NotNull
    private String sku;
    @NotNull
    private String name;
    @NotNull
    @Min(1)
    private BigDecimal price;
    @NotNull
    @ValidCurrency
    private String currency;
}
