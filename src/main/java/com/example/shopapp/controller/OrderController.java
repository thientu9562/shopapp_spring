package com.example.shopapp.controller;

import com.example.shopapp.DTO.OrderDTO;
import com.example.shopapp.DTO.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    @PostMapping("")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO orderDTo, BindingResult result) {
        try {
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            return ResponseEntity.ok("create order successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{user_id}")
    // GET http://localhost:8081/api/v1/orders/2
    public ResponseEntity<String> getOrders(@PathVariable("user_id") Long useriD){
         try {
             return ResponseEntity.ok("Lấy ra danh sách order");
         } catch (Exception e) {
             return ResponseEntity.badRequest().body(e.getMessage());
         }
    }

    @PutMapping("/{id}")
    // PUT http://localhost:8081/api/v1/orders/2
    public ResponseEntity<?> updateOrder(@Valid @PathVariable("id") Long orderId,
                                                @Valid @RequestBody OrderDTO orderDTO, BindingResult result) {
        try {
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            return ResponseEntity.ok("Cập nhật thông tin order by user ID: " + orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        // Xóa mềm => cập nhật active = false
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted with id:" + id);
    }
}
