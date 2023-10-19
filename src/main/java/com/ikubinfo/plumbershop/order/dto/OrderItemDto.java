package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.product.dto.ProductDto;
import lombok.Data;

@Data
public class OrderItemDto {

    private ProductDto product;
    private Integer amount;
}
