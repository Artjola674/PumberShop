package com.ikubinfo.plumbershop.order.controller;

import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.dto.OrderRequest;
import com.ikubinfo.plumbershop.order.service.OrderService;
import com.ikubinfo.plumbershop.security.CurrentUser;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "create a new order")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                @CurrentUser CustomUserDetails loggedUser) throws DocumentException, IOException, MessagingException {
        return ResponseEntity.ok(orderService.save(orderDto, loggedUser));
    }

    @PostMapping("/getAll")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get all orders")
    public ResponseEntity<Page<OrderDto>> getAllOrders(@Valid @RequestBody OrderRequest request,
                                                      @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(orderService.getAllOrders(loggedUser,request));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id,
                                                 @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(orderService.getById(id,loggedUser));
    }

    @Hidden
    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete order by ID")
    public ResponseEntity<String> deleteOrderById(@PathVariable String id,
                                                 @CurrentUser CustomUserDetails loggedUser){
        return ResponseEntity.ok(orderService.deleteById(id,loggedUser));
    }
}
