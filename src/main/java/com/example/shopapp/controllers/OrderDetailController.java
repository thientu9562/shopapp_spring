package com.example.shopapp.controllers;

import com.example.shopapp.dtos.OrderDetailDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orderDetails")
public class OrderDetailController {

    // Add new order detail
    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO,
                                               BindingResult result) {
        // Display error message
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok("Create orderDetail successfully");
    }

    //Get order detail
    @GetMapping("/{id}")
    public ResponseEntity<String> getOrderDetail(@PathVariable("id") Long orderDetailId) {
        return ResponseEntity.ok(String.format("Get orderDetail %d", orderDetailId));
    }

    // Get list orderDetail of 1 order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?>  getOrderDetails(@PathVariable("orderId") Long orderId) {

        return ResponseEntity.ok("Get orderDetails with ID: " + orderId);
    }

    //Update orderDetail
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(@Valid @PathVariable("id") Long orderDetailId,
                                               @RequestBody OrderDetailDTO newOrderDetailDTO,
                                               BindingResult result) {
        // Display error message
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok("updateOrderDetail ID " + orderDetailId + "with:" + newOrderDetailDTO);
    }

    // Delete orderDetail
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable("id") Long orderDetailId) {
        return ResponseEntity.noContent().build();
    }
}
