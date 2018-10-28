package com.geocom.orders.service;

import com.geocom.orders.api.ProductDTO;

public interface ProductService {

    ProductDTO createProduct(ProductDTO product);
    ProductDTO getProduct(String sku);
    ProductDTO updateProduct(String sku, ProductDTO product);
    void deleteProduct(String sku);

}
