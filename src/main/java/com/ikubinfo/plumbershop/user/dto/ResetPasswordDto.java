package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.annotation.Password;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @Password(allowNull = false)
    private String newPassword;
    @Password(allowNull = false)
    private String confirmPassword;
}
