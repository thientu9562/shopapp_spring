package com.example.shopapp.services.products;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Category;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class ProductService implements IProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        // Check category is existing
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(()-> new DataNotFoundException("Not find category with id: "+ productDTO.getCategoryId()));
        //Convert DTO -> model
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .category(existingCategory)
                .build();
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(long productId) throws Exception {
        return productRepository.findById(productId).orElseThrow(()-> new DataNotFoundException("Can't find product with id: " + productId));
    }

    @Override
    public Page<Product> getAllProducts(PageRequest pageRequest) {
        // Get list product
        return productRepository.findAll(pageRequest);
    }

    @Override
    public Product updateProduct(long id, ProductDTO productDTO) throws Exception {
        Product existingProduct = getProductById(id);
        if(existingProduct != null) {
            // Check category is existing
            Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(()-> new DataNotFoundException("Not find category with id: "+ productDTO.getCategoryId()));
            existingProduct.setName(productDTO.getName());
            existingProduct.setCategory(existingCategory);
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setThumbnail(productDTO.getThumbnail());
            return productRepository.save(existingProduct);
        }
        return null;
    }

    @Override
    public void deleteProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isPresent()) {
            productRepository.deleteById(id);
        }
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
}
