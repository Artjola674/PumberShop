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
    @Pattern(regexp = "(^ADMIN$|^SELLER$|^PLUMBER$|^USER$)",message = "Role must be 'ADMIN', 'SELLER', 'USER' or 'PLUMBER'")
    private String role;
    private String password;
    @Pattern(regexp = "(^DEPARTMENT_ONE$|^DEPARTMENT_TWO$)",message = "Role must be 'DEPARTMENT_ONE', 'DEPARTMENT_TWO'")
    private String department;
    private String phone;
    private Address address;

}
