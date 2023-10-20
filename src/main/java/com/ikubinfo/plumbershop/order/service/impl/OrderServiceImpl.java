package com.ikubinfo.plumbershop.order.service.impl;

import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.mapper.OrderMapper;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import com.ikubinfo.plumbershop.order.service.OrderService;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;

    public OrderServiceImpl(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.orderMapper = Mappers.getMapper((OrderMapper.class));
    }

    @Override
    public OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser) {
        OrderDocument orderDocument = orderMapper.toOrderDocument(orderDto);

        double totalPrice = calculateTotalOrderPrice(orderDocument);

        double buyingPriceSum = calculateTotalProductsBuyingPrice(orderDocument);

        if (UtilClass.userHasGivenRole(loggedUser, Role.PLUMBER)
                && orderDocument.getCustomer() != null) {
            totalPrice = totalPrice *
                    (1-orderDocument.getCustomer().getDiscountPercentage()/100);
        }

        double earnings = totalPrice - buyingPriceSum;

        orderDocument.setCustomer(userService.getUserByEmail(loggedUser.getEmail()));

        orderDocument.setTotalPrice(totalPrice);
        orderDocument.setEarnings(earnings);
        orderDocument.setDate(LocalDate.now());

        OrderDocument savedOrder = orderRepository.save(orderDocument);

        return orderMapper.toOrderDto(savedOrder);
    }

    private double calculateTotalProductsBuyingPrice(OrderDocument orderDocument) {
        return orderDocument.getOrderItems().stream()
                .mapToDouble(orderItemDocument ->
                        orderItemDocument.getAmount()
                                * orderItemDocument.getProduct().getBuyingPrice())
                .sum();
    }

    private double calculateTotalOrderPrice(OrderDocument orderDocument) {
        return orderDocument.getOrderItems()
                .stream()
                .mapToDouble(orderItemDocument ->
                        orderItemDocument.getAmount()
                                * orderItemDocument.getProduct().getSellingPrice())
                .sum();
    }
}
