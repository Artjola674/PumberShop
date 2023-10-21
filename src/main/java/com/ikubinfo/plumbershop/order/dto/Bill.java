package com.ikubinfo.plumbershop.order.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bill {
    private String fileName;
    private String fileLocation;
}
