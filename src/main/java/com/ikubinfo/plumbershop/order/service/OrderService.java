package com.ikubinfo.plumbershop.order.service;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.dto.OrderRequest;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;

import java.io.IOException;

public interface OrderService {
    OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser) throws DocumentException, IOException, MessagingException;

    Page<OrderDto> getAllOrders(CustomUserDetails loggedUser, OrderRequest request);

    OrderDto getById(String id, CustomUserDetails loggedUser);

    String deleteById(String id, CustomUserDetails loggedUser);

}
