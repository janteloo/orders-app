package com.geocom.orders.repository;

import com.geocom.orders.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findAllByOrderIdIn(List<Long> orderIds);

    Optional<Order> findByOrderId(Long orderId);

    List<Order> findByProductsProductId(String id);
}
