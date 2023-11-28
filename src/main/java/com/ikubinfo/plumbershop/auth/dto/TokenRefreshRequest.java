package com.ikubinfo.plumbershop.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshRequest {
    @NotNull(message = INPUT_NOT_NULL)
    private String refreshToken;
}
