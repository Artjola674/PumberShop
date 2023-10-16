package com.ikubinfo.plumbershop.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh Token should not be empty. ")
    private String refreshToken;
}
