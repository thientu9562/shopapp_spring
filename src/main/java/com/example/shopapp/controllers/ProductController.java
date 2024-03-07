package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import jakarta.validation.Valid;
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
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {

    // show all categories
    //http://localhost:8081/api/v1/products?page=1&limit=10
    @GetMapping("")
    public ResponseEntity<String> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        return ResponseEntity.ok(String.format("getProduct: page = %d, limit= %d", page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok("Get product id:" + id);
    }


    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@Valid @ModelAttribute ProductDTO productDTO,
                                           BindingResult result) {
        try {
            // Check error
            if (result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            List<MultipartFile> files = productDTO.getFiles();
            files = files == null ? new ArrayList<MultipartFile>() : files;
            for (MultipartFile file : files) {
                // Kiểm tra kích thước và định dạng
                if (file.getSize() == 0) {
                    continue;
                }
                if (file.getSize() > 10 * 1024 * 1024) { // 10MB
//                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
//                "File is too large! Maximum size is 10MB");
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File is too large! Maximum size is 10MB");
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("File must be an image!");
                }
                // Lưu file và cập nhật thumbnail trong Dto
                String filename = storeFile(file);
                // lưu vào table products
                // Lưu vào bảng product_image
            }

            return ResponseEntity.ok("Product create successfully!" + productDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // store image
    private String storeFile(MultipartFile file) throws IOException {
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
    public ResponseEntity<String> updateProduct(@PathVariable("id") String productId) {
        return ResponseEntity.ok("this is updateCategory " + productId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted with id:" + id);
    }
}