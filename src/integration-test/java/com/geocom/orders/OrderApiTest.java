package com.geocom.orders;

import com.geocom.orders.api.OrderDTO;
import com.geocom.orders.api.ProductCountDTO;
import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.repository.OrderRepository;
import com.geocom.orders.repository.ProductRepository;
import com.geocom.orders.repository.SequenceCounterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:test.properties")
public class OrderApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SequenceCounterRepository sequenceCountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Mapper objectMapper;

    @Test
    public void createOrder() {
        ResponseEntity<OrderDTO> response = restTemplate.postForEntity("/api/order", null, OrderDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.CREATED);
        assert response.getBody().getOrderId() != null;

        Optional<Order> order = orderRepository.findByOrderId(response.getBody().getOrderId());

        assert order.isPresent();

    }

    @Test
    public void getOrder() {

        OrderDTO order = getDefaultOrder();

        orderRepository.save(objectMapper.mapOrder(order));

        ResponseEntity<OrderDTO> response = restTemplate.getForEntity("/api/order/" + order.getOrderId(), OrderDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody() != null;
        assert response.getBody().getOrderId().equals(order.getOrderId());
    }


    @Test
    public void addProductToOrder() {

        OrderDTO order = getDefaultOrder();
        orderRepository.save(objectMapper.mapOrder(order));

        ProductDTO product = getDefaultProduct();
        productRepository.save(objectMapper.mapObject(product, new Product()));

        String uri = "/api/order/" + order.getOrderId() + "/product/" + product.getSku();

        ResponseEntity<OrderDTO> response = restTemplate.postForEntity(uri, null, OrderDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody() != null;
        assert response.getBody().getOrderTotal().equals(product.getPrice());

        //Add again the product
        response = restTemplate.postForEntity(uri, null, OrderDTO.class);
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody() != null;
        assert response.getBody().getOrderTotal().equals(product.getPrice().multiply(new BigDecimal(2)));

    }

    @Test
    public void updateProductCount() {

        OrderDTO order = getDefaultOrder();
        ProductDTO product = getDefaultProduct();

        productRepository.save(objectMapper.mapObject(product, new Product()));
        orderRepository.save(objectMapper.mapOrder(order));

        String uri = "/api/order/" + order.getOrderId() + "/product/" + product.getSku();

        //Adds product to order
        restTemplate.postForEntity(uri, null, OrderDTO.class);

        ProductCountDTO productCountDTO = new ProductCountDTO();
        productCountDTO.setCount(5);

        //Updates product quantity
        restTemplate.put(uri, productCountDTO);

        Optional<Order> savedOrder = orderRepository.findByOrderId(order.getOrderId());

        assert savedOrder.isPresent();

        Order saved = savedOrder.get();
        assert saved.getOrderTotal().equals(product.getPrice().multiply(new BigDecimal(5)));
    }


    @Test
    public void deleteOrderProduct() {
        OrderDTO order = getDefaultOrder();
        ProductDTO product = getDefaultProduct();

        productRepository.save(objectMapper.mapObject(product, new Product()));
        orderRepository.save(objectMapper.mapOrder(order));

        String uri = "/api/order/" + order.getOrderId() + "/product/" + product.getSku();

        //Adds product to order
        restTemplate.postForEntity(uri, null, OrderDTO.class);

        //Updates product quantity
        restTemplate.delete(uri);

        Optional<Order> savedOrder = orderRepository.findByOrderId(order.getOrderId());
        assert savedOrder.isPresent();

        Order saved = savedOrder.get();
        assert saved.getProducts().isEmpty();
    }

    @Test
    public void getAvailableCurrencies() {
        String uri = "/api/order/currencies";

        String response = restTemplate.getForObject(uri, String.class);

        assert response != null;
        assert response.contains("UYU");
        assert response.contains("USD");
    }

    @Test
    public void evaluateOrder() {
        OrderDTO order = getDefaultOrder();
        order.setOrderTotal(new BigDecimal(5500));
        orderRepository.save(objectMapper.mapOrder(order));

        String uri = "/api/order/" + order.getOrderId() + "/currency/UYU";
        ResponseEntity<OrderDTO> response = restTemplate.getForEntity(uri, OrderDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);

        OrderDTO responseOrder = response.getBody();

        assert responseOrder.getCurrency().equals("UYU");
        assert responseOrder.getOrderTotal().doubleValue() > 5500;
    }

    @Test
    public void recalculateOrder() {

        OrderDTO order = getDefaultOrder();
        ProductDTO product = getDefaultProduct();
        product.setPrice(new BigDecimal(1000));

        productRepository.save(objectMapper.mapObject(product, new Product()));
        orderRepository.save(objectMapper.mapOrder(order));

        String uri = "/api/order/" + order.getOrderId() + "/product/" + product.getSku();

        //Adds product to order
        restTemplate.postForEntity(uri, null, OrderDTO.class);

        uri = "/api/order/" + order.getOrderId() + "/currency/UYU";
        restTemplate.put(uri, null);

        Optional<Order> savedOrder = orderRepository.findByOrderId(order.getOrderId());

        assert savedOrder.isPresent();

        Order saved = savedOrder.get();
        assert saved.getCurrency().equals("UYU");
        assert saved.getOrderTotal().doubleValue() > product.getPrice().doubleValue();
    }



    private OrderDTO getDefaultOrder() {
       return  OrderDTO.builder()
                .orderTotal(new BigDecimal(0))
                .orderId(1L)
                .currency("USD")
                .products(new ArrayList<>()).build();
    }

    private ProductDTO getDefaultProduct() {
        return ProductDTO.builder().sku("PU-SKU1")
                .name("Watch")
                .price(new BigDecimal(500))
                .currency("USD").build();
    }

}
