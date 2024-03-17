package com.example.shopapp.services.orderDetail;

import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.responses.OrderDetailResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailService implements IOrderDetailService{
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public OrderDetailService(OrderDetailRepository orderDetailRepository,
                              OrderRepository orderRepository,
                              ProductRepository productRepository,
                              ModelMapper modelMapper) {
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public OrderDetailResponse createOrderDetail(OrderDetailDTO orderDetailDTO)
            throws DataNotFoundException {

        // Check order is existing
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(
                        () -> new DataNotFoundException("Cannot find order with id"
                        + orderDetailDTO.getOrderId())
                );

        // check productId is existing
        Product product = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(
                        () -> new DataNotFoundException("Cannot find product with id: "
                        + orderDetailDTO.getProductId())
                );

        // Create new OrderDetail
//        OrderDetail orderDetail = OrderDetail.builder()
//                .order(order)
//                .product(product)
//                .price(orderDetailDTO.getPrice())
//                .numberOfProduct((orderDetailDTO.getNumberOfProduct()))
//                .totalMoney(orderDetailDTO.getTotalMoney())
//                .color(orderDetailDTO.getColor())
//                .build();

        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        // define reflect clas DTO -> class model
        modelMapper.typeMap(OrderDetailDTO.class, OrderDetail.class)
                .addMappings(mapper -> {
                    mapper.skip(OrderDetail::setId);
                });

        // Initial new orderDetail obj
        OrderDetail orderDetail = new OrderDetail();

        // Reflect data from dto to model
        modelMapper.map(orderDetailDTO, orderDetail);
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);

        // Save to DB
        orderDetailRepository.save(orderDetail);
        return modelMapper.map(orderDetail, OrderDetailResponse.class);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderDetailId) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow(
                () -> new DataNotFoundException("Can not find orderDetail with id: "
                + orderDetailId)
        );
        return modelMapper.map(orderDetail, OrderDetailResponse.class);
    }

    @Override
    public OrderDetailResponse updateOrderDetail(Long orderDetailId, OrderDetailDTO orderDetailDTO)
            throws DataNotFoundException {

        // check orderId is existing in DB
        OrderDetail existingOrderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow(
                () -> new DataNotFoundException("Cannot find orderDetail with id: "
                + orderDetailId)
        );

        // Check order is existing
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(
                        () -> new DataNotFoundException("Cannot find order with id"
                                + orderDetailDTO.getOrderId())
                );

        // check productId is existing
        Product product = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(
                        () -> new DataNotFoundException("Cannot find product with id: "
                                + orderDetailDTO.getProductId())
                );
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        modelMapper.getConfiguration().setAmbiguityIgnored(true);
//
//        // Define reflect DTO to Model
//        modelMapper.typeMap(OrderDetailDTO.class, OrderDetail.class)
//                .addMappings(mapper -> {
//                    mapper.skip(OrderDetail::setId);
//                });
//
//        // Map data
//        modelMapper.map(orderDetailDTO, existingOrderDetail);

        existingOrderDetail.setColor(orderDetailDTO.getColor());
        existingOrderDetail.setNumberOfProduct(orderDetailDTO.getNumberOfProduct());
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setOrder(order);
        existingOrderDetail.setProduct(product);
        // Handing update
        orderDetailRepository.save(existingOrderDetail);

        return modelMapper.map(existingOrderDetail, OrderDetailResponse.class);
    }

    @Override
    public void deleteOrderDetail(Long orderDetailId) {
        orderDetailRepository.deleteById(orderDetailId);
    }

    @Override
    public List<OrderDetailResponse> getOrderDetails(Long orderId) {
        List<OrderDetail> orderDetailList = orderDetailRepository.findByOrderId(orderId);
        return orderDetailList.stream()
                .map(orderDetail -> modelMapper.map(orderDetail, OrderDetailResponse.class)).collect(Collectors.toList());
    }
}
