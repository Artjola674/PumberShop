package com.ikubinfo.plumbershop.common.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Filter {
    private int pageNumber = 0;
    private int pageSize = 5;
    @Pattern(regexp = "(^DESC$|^ASC$)", message = "Sort type must be 'DESC' or 'ASC'")
    private String sortType = "DESC";
    private String sortBy = "ID";

}
