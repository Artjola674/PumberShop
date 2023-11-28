package com.ikubinfo.plumbershop.common.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PageParams {
    private int pageNumber = 0;
    private int pageSize = 5;
    @Pattern(regexp = "(^DESC$|^ASC$)", message = "Sort type must be 'DESC' or 'ASC'")
    private String sortType = "DESC";
    private String sortBy = "id";

}
