package com.ikubinfo.plumbershop.product.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import lombok.Data;

import java.util.Map;

@Data
public class ProductDto extends BaseDto {

    private String name;
    private String description;
    private Long buyingPrice;
    private Long sellingPrice;
    private Integer count;
    private String code;

    private Map<String,String> attributes;
}
