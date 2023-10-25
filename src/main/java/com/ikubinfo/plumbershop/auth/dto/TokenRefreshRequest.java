package com.ikubinfo.plumbershop.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class TokenRefreshRequest {
    @NotNull(message = INPUT_NOT_NULL)
    private String refreshToken;
}
