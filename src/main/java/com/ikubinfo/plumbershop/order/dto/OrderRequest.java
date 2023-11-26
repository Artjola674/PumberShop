package com.ikubinfo.plumbershop.order.dto;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.Date;

@Data
public class OrderRequest {
    private Date fromDate;
    private Date toDate;
    private String customerId;
    @Valid
    private PageParams pageParams;
}
