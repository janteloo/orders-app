package com.geocom.orders.services;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.exception.BadRequestException;
import com.geocom.orders.exception.ResourceNotFoundException;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.model.ProductCount;
import com.geocom.orders.repository.OrderRepository;
import com.geocom.orders.repository.ProductRepository;
import com.geocom.orders.service.FixerService;
import com.geocom.orders.service.SequenceCounterService;
import com.geocom.orders.service.impl.OrderServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    private Mapper objectMapper = new Mapper();

    @Mock
    private SequenceCounterService sequenceCounterService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FixerService fixerService;

    @Mock
    private MessageConfiguration message;
    private OrderServiceImpl orderService;
    OrderDTO order;
    ProductDTO product;

    @Before
    public void setup() {
        orderService = new OrderServiceImpl(objectMapper, sequenceCounterService, orderRepository,
                productRepository, fixerService, message);

        order = OrderDTO.builder()
                .orderTotal(new BigDecimal(0))
                .orderId(1L)
                .currency("USD")
                .products(new ArrayList<>()).build();

        product = ProductDTO.builder().sku("PU-SKU1")
                .name("Watch")
                .price(new BigDecimal(500))
                .currency("USD").build();

        Field field = ReflectionUtils.findField(OrderServiceImpl.class, "availableCurrencies");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, orderService, "UYU,USD,EUR");

        when(message.getMessage(Mockito.anyString())).thenReturn("Message");
    }


    @Test
    public void createOrder() {
        when(sequenceCounterService.getNextOrderIdSequence()).thenReturn(1L);
        when(orderRepository.save(Mockito.any())).thenReturn(objectMapper.mapOrder(order));
        orderService.createOrder();

        verify(orderRepository, times(1)).save(Mockito.any());
    }

    @Test
    public void addProductToOrder() {
        Product productEntity = objectMapper.mapObject(product, new Product());
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));

        Order orderEntity = objectMapper.mapOrder(order);
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));

        OrderDTO orderReturned = orderService.addProductToOrder(order.getOrderId(), product.getSku());
        orderReturned = orderService.addProductToOrder(orderReturned.getOrderId(), product.getSku());
        orderReturned = orderService.addProductToOrder(orderReturned.getOrderId(), product.getSku());

        assert orderReturned.getOrderTotal().equals(new BigDecimal(1500));
        assert !orderReturned.getProducts().isEmpty();
        assert orderReturned.getProducts().get(0).getCount().equals(3);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void addProductToOrderEmpty() {
       orderService.addProductToOrder(100L, "NULL-PRODUCT");
    }

    @Test
    public void deleteProductFromOrder() {

        product.setPrice(new BigDecimal(750));

        Product productEntity = objectMapper.mapObject(product, new Product());
        Order orderEntity = objectMapper.mapOrder(order);

        ProductCount count = new ProductCount();
        count.setCount(2);
        count.setProduct(productEntity);

        List<ProductCount> counts = new ArrayList<>();
        counts.add(count);

        orderEntity.setProducts(counts);
        orderEntity.setOrderTotal(new BigDecimal(1500));

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));

        orderService.deleteProductFromOrder(order.getOrderId(), product.getSku());

        ArgumentCaptor<Order> argument = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(argument.capture());

        Order orderSaved = argument.getValue();
        assert orderSaved.getOrderId().equals(order.getOrderId());
        assert orderSaved.getProducts().isEmpty();
        assert orderSaved.getOrderTotal().equals(new BigDecimal(0));
    }


    @Test(expected = ResourceNotFoundException.class)
    public void deleteProductFromOrderEmpty() {
        orderService.deleteProductFromOrder(100L, "NULL-PRODUCT");
    }

    @Test
    public void updateOrderCurrency() {

        Product productEntity = objectMapper.mapObject(product, new Product());
        Order orderEntity = objectMapper.mapOrder(order);

        ProductCount count = new ProductCount();
        count.setCount(5);
        count.setProduct(productEntity);

        List<ProductCount> counts = new ArrayList<>();
        counts.add(count);

        orderEntity.setProducts(counts);
        orderEntity.setOrderTotal(new BigDecimal(2500));

        BigDecimal dolarPesoValue = new BigDecimal(33);
        BigDecimal calculatedConvertion = new BigDecimal(500).multiply(dolarPesoValue);

        when(fixerService.convert("USD", "UYU", new BigDecimal(500)))
        .thenReturn(calculatedConvertion);

        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));
        orderService.updateOrderCurrency(order.getOrderId(), "UYU");

        ArgumentCaptor<Order> argument = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(argument.capture());

        Order orderSaved = argument.getValue();
        assert orderSaved.getOrderId().equals(order.getOrderId());
        assert !orderSaved.getProducts().isEmpty();
        assert orderSaved.getOrderTotal().equals(calculatedConvertion.multiply(new BigDecimal(count.getCount())));
        assert orderSaved.getCurrency().equals("UYU");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateOrderCurrencyEmpty() {
        orderService.updateOrderCurrency(100L, "NULL-PRODUCT");
    }

    @Test(expected = BadRequestException.class)
    public void updateOrderCurrencyInvalidCurrency() {
        Order orderEntity = objectMapper.mapOrder(order);
        when(orderRepository.findByOrderId(100L)).thenReturn(Optional.of(orderEntity));
        orderService.updateOrderCurrency(100L, "NULL-PRODUCT");
    }

    @Test
    public void recalculateOrderCurrency() {
        Product productEntity = objectMapper.mapObject(product, new Product());
        Order orderEntity = objectMapper.mapOrder(order);

        ProductCount count = new ProductCount();
        count.setCount(5);
        count.setProduct(productEntity);

        List<ProductCount> counts = new ArrayList<>();
        counts.add(count);

        orderEntity.setCurrency("UYU");
        orderEntity.setProducts(counts);
        orderEntity.setOrderTotal(new BigDecimal(2500));

        BigDecimal calculatedConvertion = covertToPeso(new BigDecimal(500));

        when(fixerService.convert("USD", "UYU", new BigDecimal(500))).thenReturn(calculatedConvertion);

        when(orderRepository.findAllByOrderIdIn(Arrays.asList(order.getOrderId()))).thenReturn(Arrays.asList(orderEntity));

        orderService.recalculateOrders(Arrays.asList(order.getOrderId()));

        ArgumentCaptor<Order> argument = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(argument.capture());

        Order orderSaved = argument.getValue();
        assert orderSaved.getOrderId().equals(order.getOrderId());
        assert !orderSaved.getProducts().isEmpty();
        assert orderSaved.getOrderTotal().equals(calculatedConvertion.multiply(new BigDecimal(count.getCount())));
    }

    @Test
    public void updateProductCount() {

        Product productEntity = objectMapper.mapObject(product, new Product());
        Order orderEntity = objectMapper.mapOrder(order);

        ProductCount count = new ProductCount();
        count.setCount(1);
        count.setProduct(productEntity);

        List<ProductCount> counts = new ArrayList<>();
        counts.add(count);

        orderEntity.setProducts(Arrays.asList(count));
        orderEntity.setOrderTotal(product.getPrice());

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));

        OrderDTO orderReturned = orderService.updateProductCount(order.getOrderId(), product.getSku(), 10);

        assert orderReturned != null;
        assert orderReturned.getOrderTotal().equals(product.getPrice().multiply(new BigDecimal(10)));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateProductCountEmpty() {
        orderService.updateProductCount(null, "TEST-PRODUCT", 4);
    }

    @Test
    public void getOrderInAnotherCurrency() {

        Product productEntity = objectMapper.mapObject(product, new Product());
        Order orderEntity = objectMapper.mapOrder(order);

        ProductCount count = new ProductCount();
        count.setCount(1);
        count.setProduct(productEntity);

        List<ProductCount> counts = new ArrayList<>();
        counts.add(count);

        orderEntity.setProducts(Arrays.asList(count));
        orderEntity.setOrderTotal(product.getPrice());

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));

        OrderDTO orderReturned = orderService.updateProductCount(order.getOrderId(), product.getSku(), 10);

        BigDecimal calculatedConvertion = covertToPeso(orderReturned.getOrderTotal());

        when(fixerService.convert("USD", "UYU", orderReturned.getOrderTotal())).thenReturn(calculatedConvertion);
        OrderDTO orderInAnotherCurrency = orderService.getOrderInAnotherCurrency(order.getOrderId(), "UYU");

        assert orderInAnotherCurrency != null;
        assert orderInAnotherCurrency.getOrderTotal().equals(calculatedConvertion);
    }

    @Test(expected = BadRequestException.class)
    public void getOrderInAnotherCurrencyInvalidCurrency() {
        Order orderEntity = objectMapper.mapOrder(order);
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(orderEntity));
        orderService.getOrderInAnotherCurrency(order.getOrderId(), "NON");
    }

    public BigDecimal covertToPeso(BigDecimal dollarValue) {
        BigDecimal dollarPesoValue = new BigDecimal(33);
        return dollarValue.multiply(dollarPesoValue);
    }
}
