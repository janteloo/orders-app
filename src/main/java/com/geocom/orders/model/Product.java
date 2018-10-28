package com.geocom.orders.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotNull
    private String sku;
    @NotNull
    private String name;
    @NotNull
    private BigDecimal price;
    @NotNull
    private String currency;
}

