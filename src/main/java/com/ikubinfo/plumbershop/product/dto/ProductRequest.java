package com.ikubinfo.plumbershop.product.dto;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ProductRequest {

    @Valid
    private PageParams pageParams;
    private String code;
    private String name;
}
