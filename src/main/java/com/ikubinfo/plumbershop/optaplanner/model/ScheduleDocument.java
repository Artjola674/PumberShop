package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.common.model.BaseDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Data;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@PlanningSolution
@Data
@Document
public class ScheduleDocument extends BaseDocument {

    @ProblemFactCollectionProperty
    List<SellerAvailabilityDocument> availabilityList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    List<UserDocument> employeeList;

    @PlanningEntityCollectionProperty
    List<ShiftDocument> shiftList;

    @PlanningScore
    HardSoftScore score;

}