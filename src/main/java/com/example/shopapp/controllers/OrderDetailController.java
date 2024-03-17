package com.example.shopapp.controllers;

import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.responses.OrderDetailResponse;
import com.example.shopapp.responses.OrderResponse;
import com.example.shopapp.services.orderDetail.OrderDetailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orderDetails")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;
    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }
    // Add new order detail
    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO,
            BindingResult result) {

        // Display error message
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors()
                                                .stream()
                                                .map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // create orderDetail
        try {
            OrderDetailResponse orderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
            return ResponseEntity.ok(orderDetail);

        } catch (Exception e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Get order detail
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("id") Long orderDetailId) {

        try {
            OrderDetailResponse orderDetail = orderDetailService.getOrderDetail(orderDetailId);
            return ResponseEntity.ok(orderDetail);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get list orderDetail of 1 order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?>  getOrderDetails(@PathVariable("orderId") Long orderId) {

        List<OrderDetailResponse> orderDetailList = orderDetailService.getOrderDetails(orderId);

        return ResponseEntity.ok(orderDetailList);
    }

    //Update orderDetail
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(@Valid @PathVariable("id") Long orderDetailId,
                                               @RequestBody OrderDetailDTO orderDetailDTO,
                                               BindingResult result) {
        // Display error message
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // Update orderDetail
        try {
            OrderDetailResponse orderResponse = orderDetailService.updateOrderDetail(orderDetailId, orderDetailDTO);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // Delete orderDetail
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable("id") Long orderDetailId) {
        orderDetailService.deleteOrderDetail(orderDetailId);
        return ResponseEntity.noContent().build();
    }
}
