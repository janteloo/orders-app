package com.geocom.orders.controller;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.api.ProductCountDTO;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.exception.BadRequestException;
import com.geocom.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/order")
public class OrderController {

    @Value("${app.currency.supported.collection}")
    private String availableCurrencies;

    private OrderService orderService;
    private MessageConfiguration message;

    @Autowired
    public OrderController(OrderService orderService,
                           MessageConfiguration message) {
        this.orderService = orderService;
        this.message = message;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder() {
        OrderDTO order = orderService.createOrder();
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable("orderId") String orderId) {
        Long id = parseOrderId(orderId);
        OrderDTO order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("{orderId}/product/{productSku}")
    public ResponseEntity<OrderDTO> addProductToOrder(@PathVariable("orderId") String orderId, @PathVariable("productSku") String sku) {
        Long id = parseOrderId(orderId);
        OrderDTO order = orderService.addProductToOrder(id, sku);
        return ResponseEntity.ok(order);
    }

    @PutMapping("{orderId}/product/{productSku}")
    public ResponseEntity<OrderDTO> updateProductCount(@PathVariable("orderId") String orderId,
                                                       @PathVariable("productSku") String sku,
                                                       @Valid @RequestBody ProductCountDTO productCount) {
        Long id = parseOrderId(orderId);
        OrderDTO order = orderService.updateProductCount(id, sku, productCount.getCount());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("{orderId}/product/{productSku}")
    public ResponseEntity<OrderDTO> deleteOrderProduct(@PathVariable("orderId") String orderId, @PathVariable("productSku") String sku) {
        Long id = parseOrderId(orderId);
        orderService.deleteProductFromOrder(id, sku);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("currencies")
    public ResponseEntity<Map<String, String>> getAvailableCurrencies() {
        Map<String, String> map = new HashMap<>();
        map.put("currencies", availableCurrencies);
        return ResponseEntity.ok(map);
    }

    @GetMapping("{orderId}/currency/{currency}")
    public ResponseEntity<OrderDTO> evaluateOrder(@PathVariable("orderId") String orderId, @PathVariable("currency") String currency) {
        Long id = parseOrderId(orderId);
        validateCurrency(currency);
        OrderDTO order = orderService.getOrderInAnotherCurrency(id, currency.toUpperCase());
        return ResponseEntity.ok(order);
    }

    @PutMapping("{orderId}/currency/{currency}")
    public ResponseEntity<OrderDTO> recalculateOrder(@PathVariable("orderId") String orderId, @PathVariable("currency") String currency) {
        Long id = parseOrderId(orderId);
        validateCurrency(currency);
        OrderDTO order = orderService.updateOrderCurrency(id, currency.toUpperCase());
        return ResponseEntity.ok(order);
    }

    private Long parseOrderId(String orderId) {
        Long id = null;
        try {
            id = Long.parseLong(orderId);
        }catch(NumberFormatException e) {
            throw new BadRequestException(MessageFormat.format(message.getMessage("api.parameter.error"), "orderId"));
        }
        return id;
    }

    private void validateCurrency(String currency) {
        if(!availableCurrencies.contains(currency.toUpperCase())) {
            throw new BadRequestException(MessageFormat.format(message.getMessage("api.parameter.error"), "currency"));
        }
    }

}
