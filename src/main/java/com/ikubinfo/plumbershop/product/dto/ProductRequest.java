package com.ikubinfo.plumbershop.product.dto;

import com.ikubinfo.plumbershop.common.dto.Filter;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ProductRequest {

    @Valid
    private Filter filter;
    private String code;
    private String name;
}
