package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.dto.Filter;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class UserRequest {
    private String email;
    @Valid
    private Filter filter;
}
