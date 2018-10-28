package com.geocom.orders.services;

import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.exception.EntityAlreadyExistException;
import com.geocom.orders.exception.ResourceNotFoundException;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.model.ProductCount;
import com.geocom.orders.repository.OrderRepository;
import com.geocom.orders.repository.ProductRepository;
import com.geocom.orders.service.impl.ProductServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {


    private ProductServiceImpl productService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    private Mapper objectMapper = new Mapper();

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MessageConfiguration message;
    private ProductDTO product;
    private Product productEntity;

    @Before
    public void setup() {
        productService = new ProductServiceImpl(applicationEventPublisher, objectMapper,
                productRepository, orderRepository, message);

        product = ProductDTO.builder().sku("PU-SKU1")
                .name("Watch")
                .price(new BigDecimal(100))
                .currency("USD").build();

         productEntity = objectMapper.mapObject(product, new Product());
         productEntity.setId("TEST_ID");

    }

    @Test(expected = EntityAlreadyExistException.class)
    public void createProduct() {

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.empty());

        when(productRepository.save(Mockito.any())).thenReturn(productEntity);

        ProductDTO returnProduct = productService.createProduct(product);

        assert returnProduct != null;
        assert returnProduct.getSku().equals(product.getSku());

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        productService.createProduct(product);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getProduct() {
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        ProductDTO productReturned = productService.getProduct(product.getSku());

        assert productReturned != null;
        assert productReturned.getSku().equals(product.getSku());

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.empty());
        productService.getProduct(product.getSku());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateProduct() {
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));
        when(productRepository.save(Mockito.any())).thenReturn(productEntity);

        ProductCount productCount = new ProductCount();
        productCount.setCount(1);
        productCount.setProduct(productEntity);

        Order order = Order.builder().orderId(100L)
                .orderTotal(new BigDecimal(100))
                .currency("USD")
                .products(Arrays.asList(productCount)).build();

        when(orderRepository.findByProductsProductId(productEntity.getId())).thenReturn(Arrays.asList(order));
        ProductDTO productReturned = productService.updateProduct(product.getSku(), product);

        verify(applicationEventPublisher, times(1)).publishEvent(Mockito.any());
        assert productReturned != null;
        assert productReturned.getSku().equals(product.getSku());

        productService.updateProduct(null, product);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteProduct() {
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(productEntity));

        ProductCount productCount = new ProductCount();
        productCount.setCount(1);
        productCount.setProduct(productEntity);

        List<ProductCount> productCounts = new ArrayList<>();
        productCounts.add(productCount);

        Order order = Order.builder().orderId(100L)
                .orderTotal(new BigDecimal(100))
                .currency("USD")
                .products(productCounts).build();

        List<Order> orders = new ArrayList<>();
        orders.add(order);

        when(orderRepository.findByProductsProductId(productEntity.getId())).thenReturn(orders);
        productService.deleteProduct(product.getSku());

        when(orderRepository.findByProductsProductId(productEntity.getId())).thenReturn(null);
        productService.deleteProduct(product.getSku());

        verify(productRepository, times(2)).deleteById(productEntity.getId());
        verify(applicationEventPublisher, times(1)).publishEvent(Mockito.any());

        productService.deleteProduct(null);
    }

}
