package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Document
@PlanningEntity/*(pinningFilter = ShiftPinningFilter.class)*/
public class Shift {
    @Id
    private String id;

    @PlanningVariable
    private UserDocument seller;
    private Department department;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private SellerAvailabilityState sellerAvailabilityState;
}
