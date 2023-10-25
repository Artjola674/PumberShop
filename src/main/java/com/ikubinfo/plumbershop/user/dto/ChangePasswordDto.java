package com.ikubinfo.plumbershop.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_MESSAGE;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_REGEX;

@Data
public class ChangePasswordDto {

    @Pattern(regexp = PASS_VALIDATE_REGEX, message = PASS_VALIDATE_MESSAGE)
    private String oldPassword;
    @Pattern(regexp = PASS_VALIDATE_REGEX, message = PASS_VALIDATE_MESSAGE)
    private String newPassword;
}