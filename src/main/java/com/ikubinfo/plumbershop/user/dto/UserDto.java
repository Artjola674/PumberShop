package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.dto.BaseDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDto extends BaseDto {

    private String firstName;
    private String lastName;
    @Email
    private String email;
    @Pattern(regexp = "(^ADMIN$|^SELLER$|^PLUMBER$)",message = "Role must be 'ADMIN', 'SELLER' or 'PLUMBER'")
    private String role;

}
