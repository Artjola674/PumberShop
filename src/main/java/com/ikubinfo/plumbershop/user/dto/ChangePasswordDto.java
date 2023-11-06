package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.annotation.Password;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @Password(allowNull = false)
    private String oldPassword;
    @Password(allowNull = false)
    private String newPassword;
}
