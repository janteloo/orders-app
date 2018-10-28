package com.geocom.orders.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    @Indexed(unique = true)
    private Long orderId;
    private BigDecimal orderTotal;
    private List<ProductCount> products;
    private String currency;

    public Boolean existProduct(Product product) {
        return products.stream().anyMatch(p -> p.getProduct().getSku().equals(product.getSku()));
    }

    public void removeProduct(Product product) {
        products.removeIf(p -> p.getProduct().getSku().equals(product.getSku()));
    }

}
