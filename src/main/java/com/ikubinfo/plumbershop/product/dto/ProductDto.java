package com.ikubinfo.plumbershop.product.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class ProductDto extends BaseDto {

    @NotNull(message = INPUT_NOT_NULL)
    private String name;
    private String description;
    @NotNull(message = INPUT_NOT_NULL)
    private double buyingPrice;
    @NotNull(message = INPUT_NOT_NULL)
    private double sellingPrice;
    @NotNull(message = INPUT_NOT_NULL)
    private Integer count;
    @NotNull(message = INPUT_NOT_NULL)
    private String code;

    private Map<String,String> attributes;
}
