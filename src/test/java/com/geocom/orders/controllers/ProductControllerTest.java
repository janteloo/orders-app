package com.geocom.orders.controllers;

import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.api.UpdateProductDTO;
import com.geocom.orders.controller.ProductController;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;
    private Mapper objectMapper;
    private ProductController productController;
    private ProductDTO product;

    @Before
    public void setup() {
        objectMapper = new Mapper();
        productController = new ProductController(productService, objectMapper);

        product = ProductDTO.builder().sku("PU-SKU1")
                .name("Watch")
                .price(new BigDecimal(100))
                .currency("USD").build();
    }

    @Test
    public void createProduct() {
        when(productService.createProduct(product)).thenReturn(product);
        ResponseEntity<ProductDTO> response =  productController.createProduct(product);
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.CREATED);
        assert response.getBody().getSku().equals(product.getSku());
    }

    @Test
    public void getProduct() {
        when(productService.getProduct(product.getSku())).thenReturn(product);
        ResponseEntity<ProductDTO> response =  productController.getProduct(product.getSku());
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);
        assert response.getBody().getSku().equals(product.getSku());
    }

    @Test
    public void updateProduct() {
        UpdateProductDTO updateProduct = new UpdateProductDTO();
        updateProduct.setName("New name");
        updateProduct.setCurrency("USD");
        updateProduct.setPrice(new BigDecimal(5000));

        ProductDTO productDTO = objectMapper.mapObject(updateProduct, new ProductDTO());
        ProductDTO productWithSku = objectMapper.mapObject(updateProduct, new ProductDTO());
        productWithSku.setSku(product.getSku());

        when(productService.updateProduct(product.getSku(), productDTO)).thenReturn(productWithSku);
        ResponseEntity<ProductDTO> response = productController.updateProduct(product.getSku(), updateProduct);
        assert response != null;
        assert response.getStatusCode().equals(HttpStatus.OK);

        ProductDTO productResponse = response.getBody();

        assert productResponse.getSku().equals(product.getSku());
        assert productResponse.getName().equals(updateProduct.getName());
        assert productResponse.getCurrency().equals(updateProduct.getCurrency());
        assert productResponse.getPrice().equals(updateProduct.getPrice());

    }

    @Test
    public void deleteProduct() {
        ResponseEntity<Void> result = productController.deleteProduct(product.getSku());
        assert result != null;
        assert result.getStatusCode().equals(HttpStatus.NO_CONTENT);

    }


}
