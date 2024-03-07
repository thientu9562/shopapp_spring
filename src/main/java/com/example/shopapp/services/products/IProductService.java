package com.example.shopapp.services.products;


import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IProductService {
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException;
    Product getProductById(long id) throws Exception;
    Page<Product> getAllProducts(PageRequest pageRequest);
    Product updateProduct(long id, ProductDTO productDTO) throws Exception;
    void deleteProduct( long id);
    boolean existsByName(String name);
}
