package com.ikubinfo.plumbershop.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceIssueRequest {

    private long executionTime;
    private String methodName;
}
