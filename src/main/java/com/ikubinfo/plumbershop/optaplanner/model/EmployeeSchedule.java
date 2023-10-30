package com.ikubinfo.plumbershop.optaplanner.model;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

import java.util.List;

@PlanningSolution
public class EmployeeSchedule {

    @ProblemFactCollectionProperty
    List<SellerAvailability> availabilityList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    List<UserDocument> employeeList;

    @PlanningEntityCollectionProperty
    List<Shift> shiftList;

    @PlanningScore
    HardSoftScore score;

//    ScheduleState scheduleState;

    SolverStatus solverStatus;
}
