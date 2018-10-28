package com.geocom.orders.service.impl;

import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.event.RecalculateOrdersEvent;
import com.geocom.orders.repository.ProductRepository;
import com.geocom.orders.service.ProductService;
import com.geocom.orders.config.MessageConfiguration;
import com.geocom.orders.exception.EntityAlreadyExistException;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Order;
import com.geocom.orders.model.Product;
import com.geocom.orders.repository.OrderRepository;
import com.geocom.orders.util.ApplicationUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private ApplicationEventPublisher applicationEventPublisher;
    private Mapper objectMapper;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private MessageConfiguration message;

    private final Logger logger = Logger.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ApplicationEventPublisher applicationEventPublisher,
                              Mapper objectMapper,
                              ProductRepository productRepository,
                              OrderRepository orderRepository,
                              MessageConfiguration message) {

        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.message = message;
    }

    @Override
    public ProductDTO createProduct(ProductDTO product) {
        if(productRepository.findBySku(product.getSku()).isPresent()) {
            throw new EntityAlreadyExistException(message.getMessage("products.errors.duplicated"));
        }
        Product productEntity = objectMapper.mapObject(product, new Product());
        productRepository.save(productEntity);
        return product;
    }

    @Override
    public ProductDTO getProduct(String sku)  {
        return objectMapper.mapObject(findProduct(sku), new ProductDTO());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(String sku, ProductDTO product) {
        Product currentProduct = findProduct(sku);
        String id = currentProduct.getId();
        Product saveProduct = objectMapper.mapObject(product, new Product());
        saveProduct.setId(id);
        saveProduct.setSku(sku);
        productRepository.save(saveProduct);

        List<Order> orders = orderRepository.findByProductsProductId(saveProduct.getId());

        if(!CollectionUtils.isEmpty(orders)) {
            dispatchRecalculateOrdersEvent(orders.stream().map(Order::getOrderId).collect(Collectors.toList()));
        }

        return objectMapper.mapObject(saveProduct, new ProductDTO());
    }

    @Override
    public void deleteProduct(String sku) {
        Product product = findProduct(sku);
        List<Order> orders = orderRepository.findByProductsProductId(product.getId());
        productRepository.deleteById(product.getId());
        if(!CollectionUtils.isEmpty(orders)) {
            dispatchRecalculateOrdersEvent(orders.stream().map(Order::getOrderId).collect(Collectors.toList()));
        }
    }

    private void dispatchRecalculateOrdersEvent(List<Long> orderIds) {
        logger.info("Dispatching event to recalculate orders");
        RecalculateOrdersEvent event = new RecalculateOrdersEvent(this, orderIds);
        applicationEventPublisher.publishEvent(event);
    }

    private Product findProduct(String productSku) {
        Optional<Product> product = productRepository.findBySku(productSku);
        ApplicationUtil.checkIfPresent(product, message.getMessage("products.errors.notFound"));
        return product.get();
    }

}
