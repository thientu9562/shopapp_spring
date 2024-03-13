package com.example.shopapp.services.orders;

import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderStatus;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.responses.OrderResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService{

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final ModelMapper modelMapper;

    public OrderService(UserRepository userRepository,
                       OrderRepository orderRepository,
                        ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {

        // Check userid is existed
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Can't find user with id: " + orderDTO.getUserId()));

        // User modelMapper convert DTo -> model
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));

        // Update column in model from DTO
        Order order = new Order();
        modelMapper.map(orderDTO, order);

        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);

        // shipping_date > now
        Date shippingDate = orderDTO.getShippingDate() == null ?
                new Date() : orderDTO.getShippingDate();

        if(shippingDate.before(new Date())) {
            throw new DataNotFoundException("Date must be at least today!");
        }

        order.setActive(true);

        // save to DB
        orderRepository.save(order);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        return null;
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) {
        return null;
    }

    @Override
    public void deleteOrder(Long id) {

    }

    @Override
    public List<OrderResponse> getAllOrders(Long userId) {
        return null;
    }
}
