package com.ikubinfo.plumbershop.order.service;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface OrderService {
    OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser) throws DocumentException, IOException, MessagingException;

    OrderDto getById(String id, CustomUserDetails loggedUser);
}
