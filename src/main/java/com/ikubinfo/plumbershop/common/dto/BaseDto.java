package com.ikubinfo.plumbershop.common.dto;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;

import java.time.LocalDate;
@Data
public class BaseDto {
    private String id;
    private LocalDate createdDate;
    private LocalDate lastModifiedDate;
    private UserDocument createdBy;
    private UserDocument lastModifiedBy;
    private boolean deleted;
}
