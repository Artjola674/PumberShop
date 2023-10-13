package com.ikubinfo.plumbershop.common.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Filter {
    private int pageNumber;
    private int pageSize;
    @Pattern(regexp = "(^DESC$|^ASC$)", message = "Sort type must be 'DESC' or 'ASC'")
    private String sortType;
    private String sortBy;

}
