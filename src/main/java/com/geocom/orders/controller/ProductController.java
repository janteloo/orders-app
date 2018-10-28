package com.geocom.orders.controller;

import com.geocom.orders.api.ProductDTO;
import com.geocom.orders.api.UpdateProductDTO;
import com.geocom.orders.mapper.Mapper;
import com.geocom.orders.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/product")
public class ProductController {

    private ProductService productService;
    private Mapper objectMapper;

    public ProductController(ProductService productService,
                             Mapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;

    }
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO product) {
        ProductDTO createdProduct =  productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("{sku}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable("sku") String sku) {
        ProductDTO product = productService.getProduct(sku);
        return ResponseEntity.ok(product);
    }

    @PutMapping("{sku}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable("sku") String sku, @Valid @RequestBody UpdateProductDTO product) {
        ProductDTO productReturn =  productService.updateProduct(sku, objectMapper.mapObject(product, new ProductDTO()));
        return ResponseEntity.ok(productReturn);
    }

    @DeleteMapping("{sku}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("sku") String sku) {
        productService.deleteProduct(sku);
        return ResponseEntity.noContent().build();
    }


}
