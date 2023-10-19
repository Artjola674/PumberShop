package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDto extends BaseDto {
    @NotNull
    @NotEmpty
    private List<OrderItemDto> orderItems;
    private UserDto plumber;
    private double totalPrice;
    private double earnings;
    private LocalDate date;

}
