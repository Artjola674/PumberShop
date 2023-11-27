package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String customerId;
    @Valid
    private PageParams pageParams;

}
