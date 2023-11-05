package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
@PlanningEntity
public class ShiftDocument {
    @PlanningId
    private String id;
    @PlanningVariable
    private UserDocument seller;
    private Department department;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
