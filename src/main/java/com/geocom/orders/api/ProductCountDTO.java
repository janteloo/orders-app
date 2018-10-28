package com.geocom.orders.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductCountDTO implements Serializable {

    @NotNull
    @Min(1)
    private Integer count;
    private ProductDTO product;
}
