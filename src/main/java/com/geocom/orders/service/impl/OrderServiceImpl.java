package com.geocom.orders.service.impl;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.exception.BadRequestException;
import com.geocom.orders.model.ProductCount;
import com.geocom.orders.repository.ProductRepository;
import com.geocom.orders.service.FixerService;
import com.geocom.orders.service.SequenceCounterService;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.repository.OrderRepository;
import com.geocom.orders.service.OrderService;
import com.geocom.orders.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private Mapper objectMapper;
    private SequenceCounterService sequenceCounterService;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private FixerService fixerService;
    private MessageConfiguration message;

    @Value("${app.currency.default}")
    private String defaultCurrency;

    @Value("${app.currency.supported.collection}")
    private String availableCurrencies;

    @Autowired
    public OrderServiceImpl(Mapper objectMapper,
                            SequenceCounterService sequenceCounterService,
                            OrderRepository orderRepository,
                            ProductRepository productRepository,
                            FixerService fixerService,
                            MessageConfiguration message) {

        this.objectMapper = objectMapper;
        this.sequenceCounterService = sequenceCounterService;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.fixerService = fixerService;
        this.message = message;
    }

    @Override
    public OrderDTO getOrder(Long orderId) {
        return objectMapper.mapOrder(findOrder(orderId));
    }

    @Override
    public OrderDTO createOrder() {
        Order order = Order.builder().orderId(sequenceCounterService.getNextOrderIdSequence())
                .orderTotal(new BigDecimal(0))
                .currency(defaultCurrency)
                .products(new ArrayList<>()).build();

        Order orderSaved = orderRepository.save(order);
        return objectMapper.mapObject(orderSaved, new OrderDTO());
    }

    @Override
    @Transactional
    public OrderDTO addProductToOrder(Long orderId, String productSku) {
        Product product = findProduct(productSku);
        Order order = findOrder(orderId);

        Optional<ProductCount> productCount = order.getProducts().stream()
                .filter(p -> p.getProduct().getSku().equals(productSku))
                .findFirst();

        ProductCount count = new ProductCount();
        if(productCount.isPresent()) {
            count = productCount.get();
            count.addCount();
        } else {
            count.setProduct(product);
            order.getProducts().add(count);
        }

        order = sumProductToOrder(product, 1, order);
        orderRepository.save(order);
        return objectMapper.mapOrder(order);
    }

    @Override
    @Transactional
    public void deleteProductFromOrder(Long orderId, String productSku) {
        Product product = findProduct(productSku);
        Order order = findOrder(orderId);

        order.removeProduct(product);
        order = calculateTotal(order);
        orderRepository.save(order);
    }


    @Override
    @Transactional
    public OrderDTO updateOrderCurrency(Long orderId, String currency) {
        Order order = findOrder(orderId);
        checkIfCurrencyIsValid(currency);
        if(!order.getCurrency().equals(currency)) {
            order.setCurrency(currency);
            order = calculateTotal(order);
            orderRepository.save(order);
        }
        return objectMapper.mapOrder(order);
    }


    @Override
    @Transactional
    public void recalculateOrders(List<Long> orderIds) {
        List<Order> orders = orderRepository.findAllByOrderIdIn(orderIds);
        if(!CollectionUtils.isEmpty(orders)) {
            for (Order order : orders) {
                order = calculateTotal(order);
                orderRepository.save(order);
            }
        }
    }

    @Override
    @Transactional
    public OrderDTO updateProductCount(Long orderId, String productSku, Integer count) {
        Product product = findProduct(productSku);
        Order order = findOrder(orderId);

        Optional<ProductCount> productCount = order.getProducts().stream()
                .filter(p -> p.getProduct().getSku().equals(product.getSku()))
                .findFirst();

        if(productCount.isPresent()) {
            ProductCount productCountEntity = productCount.get();
            productCountEntity.setCount(count);
            order = calculateTotal(order);
            orderRepository.save(order);
        }
        return objectMapper.mapOrder(order);
    }

    @Override
    public OrderDTO getOrderInAnotherCurrency(Long orderId, String currency) {
        Order order = findOrder(orderId);
        OrderDTO returnOrder = objectMapper.mapOrder(order);
        checkIfCurrencyIsValid(currency);

        if(!currency.equals(order.getCurrency())) {
            BigDecimal orderTotal = fixerService.convert(order.getCurrency(), currency, order.getOrderTotal());
            returnOrder.setOrderTotal(orderTotal);
            returnOrder.setCurrency(currency);
        }

        return returnOrder;
    }

    private Order calculateTotal(Order order) {
        order.setOrderTotal(new BigDecimal(0));
        List<ProductCount> products = order.getProducts();
        for(ProductCount orderProduct : products) {
            order = sumProductToOrder(orderProduct.getProduct(), orderProduct.getCount(), order);
        }
        return order;
    }

    private Order sumProductToOrder(Product product, Integer count, Order order) {
        BigDecimal productPrice = product.getPrice();
        if (!order.getCurrency().equals(product.getCurrency())) {
            productPrice = fixerService.convert(product.getCurrency(), order.getCurrency(), product.getPrice());
        }
        BigDecimal total = productPrice.multiply(new BigDecimal(count));
        total = order.getOrderTotal().add(total);
        order.setOrderTotal(total);
        return order;
    }

    private Product findProduct(String productSku) {
        Optional<Product> product = productRepository.findBySku(productSku);
        ApplicationUtil.checkIfPresent(product, message.getMessage("products.errors.notFound"));
        return product.get();
    }

    private Order findOrder(Long orderId) {
        Optional<Order> order = orderRepository.findByOrderId(orderId);
        ApplicationUtil.checkIfPresent(order, message.getMessage("orders.errors.notFound"));
        return order.get();
    }

    private void checkIfCurrencyIsValid(String currency) {
        List<String> supportedCurrencies = Arrays.asList(availableCurrencies.split(","));
        if(!supportedCurrencies.contains(currency)) {
            throw new BadRequestException(MessageFormat.format(message.getMessage("service.validation.unknown-currency"), currency));
        }
    }
}
