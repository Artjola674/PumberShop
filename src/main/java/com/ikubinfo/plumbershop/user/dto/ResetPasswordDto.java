package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.annotation.Password;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @Password
    private String newPassword;
    @Password
    private String confirmPassword;
}
