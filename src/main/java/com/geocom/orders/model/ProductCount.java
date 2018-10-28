package com.geocom.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCount {

    private Integer count = 1;
    @DBRef
    private Product product;

    public void addCount() {
        count++;
    }
}
