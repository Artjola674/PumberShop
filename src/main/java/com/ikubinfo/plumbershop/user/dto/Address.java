package com.ikubinfo.plumbershop.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Address {

    @NotNull
    private String city;
    @NotNull
    private Integer postalCode;
    @NotNull
    private String street;
}
