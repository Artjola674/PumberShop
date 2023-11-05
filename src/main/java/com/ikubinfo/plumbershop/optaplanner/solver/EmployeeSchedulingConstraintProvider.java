package com.ikubinfo.plumbershop.optaplanner.solver;

import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;

import java.time.Duration;
import java.time.LocalDateTime;

public class EmployeeSchedulingConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                requiredDepartment(constraintFactory),
                noOverlappingShifts(constraintFactory),
                oneShiftPerDay(constraintFactory),
                unavailableEmployee(constraintFactory),
                desiredDayForEmployee(constraintFactory),
                undesiredDayForEmployee(constraintFactory),
        };
    }

    Constraint requiredDepartment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ShiftDocument.class)
                .filter(shift -> !shift.getSeller().getDepartment().equals(shift.getDepartment()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing department");
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ShiftDocument.class, Joiners.equal(ShiftDocument::getSeller),
                        Joiners.overlapping(ShiftDocument::getStartDateTime, ShiftDocument::getEndDateTime))
                .penalize(HardSoftScore.ONE_HARD,
                        EmployeeSchedulingConstraintProvider::getMinuteOverlap)
                .asConstraint("Overlapping shift");
    }

    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ShiftDocument.class,
                        Joiners.equal(ShiftDocument::getSeller),
                        Joiners.equal(shift -> shift.getStartDateTime().toLocalDate()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Max one shift per day");
    }

    Constraint unavailableEmployee(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory,SellerAvailabilityState.UNAVAILABLE)
                .penalize(HardSoftScore.ONE_HARD,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Unavailable employee");
    }

    Constraint desiredDayForEmployee(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory,SellerAvailabilityState.DESIRED)
                .reward(HardSoftScore.ONE_SOFT,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Desired day for employee");
    }

    Constraint undesiredDayForEmployee(ConstraintFactory constraintFactory) {
        return getConstraintStreamWithAvailabilityIntersections(constraintFactory,SellerAvailabilityState.UNDESIRED)
                .penalize(HardSoftScore.ONE_SOFT,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Undesired day for employee");
    }


    private static BiConstraintStream<ShiftDocument, SellerAvailabilityDocument> getConstraintStreamWithAvailabilityIntersections(
            ConstraintFactory constraintFactory, SellerAvailabilityState employeeAvailabilityState) {
        return constraintFactory.forEach(ShiftDocument.class)
                .join(SellerAvailabilityDocument.class,
                        Joiners.lessThan(ShiftDocument::getStartDateTime, SellerAvailabilityDocument::getEndDateTime),
                        Joiners.greaterThan(ShiftDocument::getEndDateTime, SellerAvailabilityDocument::getStartDateTime),
                        Joiners.equal(ShiftDocument::getSeller, SellerAvailabilityDocument::getSeller))
                .filter((shift, availability) -> availability.getSellerAvailabilityState() == employeeAvailabilityState);

    }

    private static int getMinuteOverlap(ShiftDocument shift1, ShiftDocument shift2) {
        LocalDateTime shift1Start = shift1.getStartDateTime();
        LocalDateTime shift1End = shift1.getEndDateTime();
        LocalDateTime shift2Start = shift2.getStartDateTime();
        LocalDateTime shift2End = shift2.getEndDateTime();
        return (int) Duration.between((shift1Start.compareTo(shift2Start) > 0) ? shift1Start : shift2Start,
                (shift1End.compareTo(shift2End) < 0) ? shift1End : shift2End).toMinutes();
    }

    private static int getShiftDurationInMinutes(ShiftDocument shift) {
        return (int) Duration.between(shift.getStartDateTime(), shift.getEndDateTime()).toMinutes();
    }

}
