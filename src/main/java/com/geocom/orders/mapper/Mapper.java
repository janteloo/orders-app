package com.geocom.orders.mapper;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.api.ProductCountDTO;
import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.model.ProductCount;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Mapper {

    public <S, D> D mapObject(S source, D destination) {
        BeanUtils.copyProperties(source, destination);
        return destination;
    }

    public OrderDTO mapOrder(Order source) {
        List<ProductCountDTO> products = new ArrayList<>();

        for(ProductCount product : source.getProducts()) {
            ProductCountDTO productCount = ProductCountDTO.builder()
                    .product(mapObject(product.getProduct(), new ProductDTO()))
                    .count(product.getCount()).build();
            products.add(productCount);
        }

        OrderDTO order = OrderDTO.builder()
                .orderId(source.getOrderId())
                .orderTotal(source.getOrderTotal())
                .products(products)
                .currency(source.getCurrency())
                .build();

        return order;
    }

    public Order mapOrder(OrderDTO source) {
        List<ProductCount> products = new ArrayList<>();

        for(ProductCountDTO product : source.getProducts()) {
            ProductCount productCount = ProductCount.builder()
                    .product(mapObject(product.getProduct(), new Product()))
                    .count(product.getCount()).build();
            products.add(productCount);
        }

        Order order = Order.builder()
                .orderId(source.getOrderId())
                .orderTotal(source.getOrderTotal())
                .products(products)
                .currency(source.getCurrency())
                .build();

        return order;
    }

}
