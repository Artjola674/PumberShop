package com.ikubinfo.plumbershop.common.model;


import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;

@Data
public abstract class Auditable {
    @CreatedDate
    private LocalDate createdDate;
    @LastModifiedDate
    private LocalDate lastModifiedDate;
    @CreatedBy
    private UserDocument createdBy;
    @LastModifiedBy
    private UserDocument lastModifiedBy;
}
