package com.geocom.orders;

import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.api.UpdateProductDTO;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.model.Product;
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
import java.util.Optional;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:test.properties")
public class ProductApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Mapper objectMapper;

    @Test
    public void createProduct(){
        ProductDTO product = ProductDTO.builder().sku("PRODUCT-SKU")
                .name("TEST")
                .price(new BigDecimal(2500))
                .currency("UYU").build();

        ResponseEntity<ProductDTO> response = restTemplate.postForEntity("/api/product", product, ProductDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.CREATED);
        assert response.getBody().getSku().equals(product.getSku());
        assert response.getBody().getName().equals(product.getName());
        assert response.getBody().getPrice().equals(product.getPrice());

        Optional<Product> productFound = productRepository.findBySku(product.getSku());
        assert productFound.isPresent();
    }

    @Test
    public void getProduct(){
        ProductDTO product = ProductDTO.builder().sku("PRODUCT-SKU")
                .name("TEST")
                .price(new BigDecimal(2500))
                .currency("UYU").build();

        productRepository.save(objectMapper.mapObject(product, new Product()));

        ResponseEntity<ProductDTO> response = restTemplate.getForEntity("/api/product/PRODUCT-SKU", ProductDTO.class);

        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getSku().equals(product.getSku());
        assert response.getBody().getName().equals(product.getName());

    }


    @Test
    public void updateProduct(){
        ProductDTO product = ProductDTO.builder().sku("PRODUCT-SKU")
                .name("TEST")
                .price(new BigDecimal(2500))
                .currency("UYU").build();

        productRepository.save(objectMapper.mapObject(product, new Product()));

        UpdateProductDTO productUpdate = new UpdateProductDTO();
        productUpdate.setName("New Test Name");
        productUpdate.setPrice(new BigDecimal(200));
        productUpdate.setCurrency("USD");

        restTemplate.put("/api/product/PRODUCT-SKU", productUpdate);

        Optional<Product> updatedProduct = productRepository.findBySku(product.getSku());

        assert updatedProduct.isPresent();

        Product updated = updatedProduct.get();
        assert updated.getSku().equals(product.getSku());
        assert updated.getName().equals(productUpdate.getName());
        assert updated.getPrice().equals(productUpdate.getPrice());
        assert updated.getCurrency().equals(productUpdate.getCurrency());
    }


    @Test
    public void deleteProduct(){
        ProductDTO product = ProductDTO.builder().sku("PRODUCT-SKU")
                .name("TEST")
                .price(new BigDecimal(2500))
                .currency("UYU").build();

        productRepository.save(objectMapper.mapObject(product, new Product()));

        restTemplate.delete("/api/product/PRODUCT-SKU");

        Optional<Product> deletedProduct = productRepository.findBySku(product.getSku());

        assert !deletedProduct.isPresent();
    }

}
