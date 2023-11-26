package com.ikubinfo.plumbershop.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
@Builder
public class AuthRequest {
    @NotNull(message = INPUT_NOT_NULL)
    private String email;
    @NotNull(message = INPUT_NOT_NULL)
    private String password;
}
