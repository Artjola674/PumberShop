package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.dto.Filter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {
    private String email;
    @Pattern(regexp = "(^DEPARTMENT_ONE$|^DEPARTMENT_TWO$)", message = "Role must be 'DEPARTMENT_ONE', 'DEPARTMENT_TWO'")
    private String department;
    @Pattern(regexp = "(^ADMIN$|^SELLER$|^PLUMBER$|^USER$)", message = "Role must be 'ADMIN', 'SELLER', 'USER' or 'PLUMBER'")
    private String role;
    @Valid
    private Filter filter;
}
