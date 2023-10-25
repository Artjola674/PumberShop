package com.ikubinfo.plumbershop.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;

@Data
public class Address {

    @NotNull(message = INPUT_NOT_NULL)
    private String city;
    @NotNull(message = INPUT_NOT_NULL)
    private Integer postalCode;
    @NotNull(message = INPUT_NOT_NULL)
    private String street;
}
