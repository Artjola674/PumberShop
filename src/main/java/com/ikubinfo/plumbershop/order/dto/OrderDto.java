package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class OrderDto extends BaseDto {
    @NotNull(message = INPUT_NOT_NULL)
    @NotEmpty
    @Valid
    private List<OrderItemDto> orderItems;
    private UserDto customer;
    private double totalPrice;
    private double earnings;
    private LocalDate date;

}
