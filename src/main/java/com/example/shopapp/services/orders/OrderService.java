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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        LocalDate shippingDate = orderDTO.getShippingDate() == null ?
                LocalDate.now() : orderDTO.getShippingDate();

        // check shipDate must be least today
        if(shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today!");
        }

        order.setActive(true);
        order.setShippingDate(shippingDate);
        // save to DB
        orderRepository.save(order);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse getOrder(Long orderId) throws Exception {

        // Get information of order
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new DataNotFoundException("Order does not find with id: " + orderId)
        );
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse updateOrder(Long orderId, OrderDTO orderDTO)
            throws DataNotFoundException {

        // Check order is exist DB
        Order existingOrder = orderRepository.findById(orderId).orElseThrow(
                () -> new DataNotFoundException("Cannot find order with id: " + orderId)
        );

        // Check user is existing in DB
        User existingUser = userRepository.findById(orderDTO.getUserId()).orElseThrow(
                () -> new DataNotFoundException("Cannot find user with id: "
                        + orderDTO.getUserId())
        );

        // Convert DTO to Model
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));

        // Mapping
        modelMapper.map(orderDTO, existingOrder);

        // Set param
        existingOrder.setUser(existingUser);

        orderRepository.save(existingOrder);

        return modelMapper.map(existingOrder, OrderResponse.class);
    }

    @Override
    public void deleteOrder(Long id) throws DataNotFoundException {

        // Find order in DB
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Cannot find order with id: " + id)
        );

        // soft-delete
        if(order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderResponse> findByUserId(Long userId) {

        // Get list order by userId
        List<Order> orderList = orderRepository.findByUserId(userId);

        // Mapper for List order
        return orderList.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
}
