package com.ikubinfo.plumbershop.order.controller;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.service.OrderService;
import com.ikubinfo.plumbershop.security.CurrentUser;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "create a new order")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                @CurrentUser CustomUserDetails loggedUser) throws DocumentException, IOException, URISyntaxException {
        return ResponseEntity.ok(orderService.save(orderDto, loggedUser));
    }
}
