package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class OrderItemDto {

    @NotNull(message = INPUT_NOT_NULL)
    private ProductDto product;
    @NotNull(message = INPUT_NOT_NULL)
    private Integer amount;
}
