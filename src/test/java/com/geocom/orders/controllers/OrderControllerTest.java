package com.geocom.orders.controllers;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.api.ProductCountDTO;
import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.controller.OrderController;
import com.geocom.orders.exception.BadRequestException;
import com.geocom.orders.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;
    @Mock
    private MessageConfiguration message;
    private OrderController orderController;
    private OrderDTO order;
    private ProductDTO product;
    private String availableCurrencies = "UYU,USD,EUR";

    @Before
    public void setup() {
        orderController = new OrderController(orderService, message);
        order = OrderDTO.builder()
                .orderTotal(new BigDecimal(0))
                .orderId(1L)
                .currency("USD")
                .products(new ArrayList<>()).build();

        product = ProductDTO.builder().sku("PU-SKU1")
                .name("Watch")
                .price(new BigDecimal(500))
                .currency("USD").build();

        ProductCountDTO productCount = new ProductCountDTO();
        productCount.setCount(1);
        productCount.setProduct(product);

        order.getProducts().add(productCount);

        when(message.getMessage(Mockito.anyString())).thenReturn("ANY MESSAGE");

        Field field = ReflectionUtils.findField(OrderController.class, "availableCurrencies");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, orderController, availableCurrencies);
    }

    @Test
    public void createOrder() {
        when(orderService.createOrder()).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.createOrder();
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.CREATED);
        assert response.getBody().getOrderId().equals(order.getOrderId());
    }

    @Test
    public void getOrder() {
        when(orderService.getOrder(order.getOrderId())).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.getOrder(order.getOrderId().toString());
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getOrderId().equals(order.getOrderId());
        assert response.getBody().getOrderTotal().equals(order.getOrderTotal());
    }

    @Test(expected = BadRequestException.class)
    public void getOrderBadRequest() {
        orderController.getOrder("NOT A NUMBER");
    }

    @Test
    public void addProductToOrder() {
        when(orderService.addProductToOrder(order.getOrderId(), product.getSku())).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.addProductToOrder(order.getOrderId().toString(), product.getSku());

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getProducts().size() == 1;
        assert response.getBody().getOrderId().equals(order.getOrderId());
    }

    @Test
    public void updateProductCount() {

        ProductCountDTO count = new ProductCountDTO();
        count.setCount(5);

        order.getProducts().get(0).setCount(5);

        when(orderService.updateProductCount(order.getOrderId(), product.getSku(), count.getCount())).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.updateProductCount(order.getOrderId().toString(), product.getSku(), count);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getProducts().size() == 1;
        assert response.getBody().getOrderId().equals(order.getOrderId());
    }

    @Test
    public void deleteOrderProduct() {
        ResponseEntity<OrderDTO> response = orderController.deleteOrderProduct(order.getOrderId().toString(), product.getSku());
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.NO_CONTENT);
    }


    @Test
    public void getAvailableCurrencies() {
        ResponseEntity<Map<String, String>> response = orderController.getAvailableCurrencies();

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody() != null;

        Map<String, String> body = response.getBody();
        assert body.get("currencies").equals(availableCurrencies);
    }


    @Test
    public void evaluateOrder() {
        order.setCurrency("UYU");
        when(orderService.getOrderInAnotherCurrency(order.getOrderId(), "UYU")).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.evaluateOrder(order.getOrderId().toString(), "UYU");
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getOrderId().equals(order.getOrderId());
        assert response.getBody().getCurrency().equals(order.getCurrency());
    }

    @Test(expected = BadRequestException.class)
    public void evaluateOrderInvalidCurrency() {
        orderController.evaluateOrder(order.getOrderId().toString(), "TEST");
    }

    public void recalculateOrder() {
        order.setCurrency("UYU");
        when(orderService.updateOrderCurrency(order.getOrderId(), "UYU")).thenReturn(order);
        ResponseEntity<OrderDTO> response = orderController.recalculateOrder(order.getOrderId().toString(), "UYU");
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getOrderId().equals(order.getOrderId());
        assert response.getBody().getCurrency().equals(order.getCurrency());
    }



}
