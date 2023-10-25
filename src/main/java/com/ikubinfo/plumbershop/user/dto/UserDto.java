package com.ikubinfo.plumbershop.user.dto;

import com.ikubinfo.plumbershop.common.annotation.Password;
import com.ikubinfo.plumbershop.common.dto.BaseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class UserDto extends BaseDto {

    @NotNull(message = INPUT_NOT_NULL)
    private String firstName;
    @NotNull(message = INPUT_NOT_NULL)
    private String lastName;
    @Email
    @NotNull(message = INPUT_NOT_NULL)
    private String email;
    @Pattern(regexp = "(^ADMIN$|^SELLER$|^PLUMBER$|^USER$)",message = "Role must be 'ADMIN', 'SELLER', 'USER' or 'PLUMBER'")
    private String role;
    @Password(allowNull = true)
    private String password;
    @Pattern(regexp = "(^DEPARTMENT_ONE$|^DEPARTMENT_TWO$)",message = "Role must be 'DEPARTMENT_ONE', 'DEPARTMENT_TWO'")
    private String department;
    @NotNull(message = INPUT_NOT_NULL)
    @Pattern(regexp = "^(((067|068|069)\\d{7})){1}$", message = "Phone number is not valid.")
    private String phone;
    @NotNull(message = INPUT_NOT_NULL)
    @Valid
    private Address address;
    private double discountPercentage;


}
