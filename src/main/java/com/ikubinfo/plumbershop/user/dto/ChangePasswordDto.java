package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.annotation.Password;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @Password
    private String oldPassword;
    @Password
    private String newPassword;
}
