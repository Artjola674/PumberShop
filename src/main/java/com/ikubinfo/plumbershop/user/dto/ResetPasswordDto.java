package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto {
    @Password(allowNull = false)
    private String newPassword;
    @Password(allowNull = false)
    private String confirmPassword;
}
