package com.ikubinfo.plumbershop.order.service;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.net.URISyntaxException;

public interface OrderService {
    OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser) throws DocumentException, IOException, URISyntaxException;
}
