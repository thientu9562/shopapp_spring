package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.responses.ProductListresponse;
import com.example.shopapp.responses.ProductResponse;
import com.example.shopapp.services.products.ProductService;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {

     private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // show all categories
    //http://localhost:8081/api/v1/products?page=1&limit=10
    @GetMapping("")
    public ResponseEntity<ProductListresponse> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit) {

        // Create pageable, page of begin = 0
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by("createAt").descending());

        // Get all products
        Page<ProductResponse> productPage = productService.getAllProducts(pageRequest);

        // Get total page
        int totalPage = productPage.getTotalPages();

        List<ProductResponse> products = productPage.getContent();
        return ResponseEntity.ok(ProductListresponse.builder()
                        .products(products)
                        .totalPage(totalPage)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable("id") Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(ProductResponse.formProduct(product));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("")
    //Post: http://localhost:8081/api/v1/products
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO,
                                           BindingResult result) {
        try {

            // Check error
            if (result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            // Save new product
            Product newProduct = productService.createProduct(productDTO);

            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "uploads/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //Post: http://localhost:8081/api/v1/products
    public ResponseEntity<?> uploadImages(
            @PathVariable("productId") Long productId,
            @ModelAttribute("file") List<MultipartFile> files){

        try {
            Product existingProduct = productService.getProductById(productId);

            // Check file exist
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if (files.size() > ProductImage.MAXIMUM_IMAGE_PER_PRODUCT) {
                return ResponseEntity.badRequest()
                        .body(String
                                .format("You can only upload maximun %d image",
                                        ProductImage.MAXIMUM_IMAGE_PER_PRODUCT));
            }
            // List product image insert successfully
            List<ProductImage> productImageList = new ArrayList<>();

            for (MultipartFile file : files) {

                // Kiểm tra kích thước và định dạng
                if (file.getSize() == 0) {
                    continue;
                }
                if (file.getSize() > 10 * 1024 * 1024) { // 10MB
//                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
//                "File is too large! Maximum size is 10MB");
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large! Maximum size is 10MB");
                }

                // Checking contextType file is image
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image!");
                }

                // Lưu file và cập nhật thumbnail trong Dto
                String filename = storeFile(file);

                // Lưu vào bảng product_image
                ProductImage newProductImage = productService.createProductImage(existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build());
                // Add image success to List
                productImageList.add(newProductImage);
            }
            return ResponseEntity.ok(productImageList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // store image
    private String storeFile(MultipartFile file) throws IOException {
        if (file.getOriginalFilename() == null) {
            throw new IOException("Invalid image file format");
        }
        // Get ten file và đổi tên( nhằm tránh 2 user khi upload chung tên 1 file ảnh thì ảnh trước sẽ bị ghi đè)
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        // Them UUID và trước file để đảm bảo duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // Dường dãn đến thư mục muốn lưu (sd thưu viện java.nio.file)
        Path uploadDir = Paths.get("uploads");
        // Kiêm tra tồn tại or tạo thư mục
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Dường dẫn đầy đủ đến file
        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // Sao chép file vo thư mực dích
        //StandardCopyOption.REPLACE_EXISTING Nếu có thì sẽ chèn thay thế
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") Long productId,
            @RequestBody ProductDTO productDTO
            ) {
        try {
            Product updateProduct = productService.updateProduct(productId, productDTO);
            return ResponseEntity.ok(updateProduct);
        } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
           productService.deleteProduct(id);
            return ResponseEntity.ok("Delete successfully for Id: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PostMapping("/generateFakerProducts")
    public ResponseEntity<String> generateFakeProducts() {
        Faker faker = new Faker();

        for (int i = 0; i < 100; i++) {

            // Auto gen name
            String productName = faker.commerce().productName();
            if(productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float) faker.number().numberBetween(10, 100000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(2,5))
                    .build();
            try {
                productService.createProduct(productDTO);
            } catch (DataNotFoundException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("fake product created successfully");
    }
}