package com.example.shopapp.controllers;

import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.responses.OrderResponse;
import com.example.shopapp.services.orders.IOrderService;
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

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO orderDTO, BindingResult result) {
        try {
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            // Create order
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}")
    // GET http://localhost:8081/api/v1/orders/user/2
    public ResponseEntity<?> getOrders(@PathVariable("user_id") Long useriD){
         try {

             // Get list information order by userId
             List<OrderResponse> orderResponseList = orderService.findByUserId(useriD);
             return ResponseEntity.ok(orderResponseList);
         } catch (Exception e) {
             return ResponseEntity.badRequest().body(e.getMessage());
         }
    }

    @GetMapping("/{id}")
    // GET http://localhost:8081/api/v1/orders/2
    public ResponseEntity<?> getOrder(@PathVariable("id") Long orderId){
        try {

            // Get information order by id
            OrderResponse orderResponse = orderService.getOrder(orderId);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    // PUT http://localhost:8081/api/v1/orders/2
    public ResponseEntity<?> updateOrder(@Valid @PathVariable("id") Long orderId,
                                                @Valid @RequestBody OrderDTO orderDTO, BindingResult result) {
        try {

            // Display error validate
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            // Update order
            OrderResponse order = orderService.updateOrder(orderId, orderDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId) throws DataNotFoundException {
        // soft-delete => update active = false
        orderService.deleteOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body("Order deleted with id:" + orderId);
    }
}
