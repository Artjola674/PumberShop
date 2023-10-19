package com.ikubinfo.plumbershop.order.service;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.security.CustomUserDetails;

public interface OrderService {
    OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser);
}
