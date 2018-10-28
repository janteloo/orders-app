package com.geocom.orders.service;

import com.geocom.orders.api.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO getOrder(Long orderId);
    OrderDTO createOrder();
    OrderDTO addProductToOrder(Long orderId, String productSku);
    OrderDTO updateProductCount(Long orderId, String productSku, Integer count);
    void deleteProductFromOrder(Long orderId, String productSku);
    void recalculateOrders(List<Long> orderIds);
    OrderDTO getOrderInAnotherCurrency(Long orderId, String currency);
    OrderDTO updateOrderCurrency(Long orderId, String currency);
}
