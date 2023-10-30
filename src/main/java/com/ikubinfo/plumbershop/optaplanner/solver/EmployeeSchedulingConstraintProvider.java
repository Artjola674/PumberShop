package com.ikubinfo.plumbershop.optaplanner.solver;

import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailability;
import com.ikubinfo.plumbershop.optaplanner.model.Shift;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.Duration;
import java.time.LocalDateTime;

public class EmployeeSchedulingConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                requiredDepartment(constraintFactory),
                noOverlappingShifts(constraintFactory),
                atLeast10HoursBetweenTwoShifts(constraintFactory),
                oneShiftPerDay(constraintFactory),
                unavailableEmployee(constraintFactory),
                desiredDayForEmployee(constraintFactory),
                undesiredDayForEmployee(constraintFactory),
        };
    }

    private static int getMinuteOverlap(Shift shift1, Shift shift2) {
        // The overlap of two timeslot occurs in the range common to both timeslots.
        // Both timeslots are active after the higher of their two start times,
        // and before the lower of their two end times.
        LocalDateTime shift1Start = shift1.getStartDateTime();
        LocalDateTime shift1End = shift1.getEndDateTime();
        LocalDateTime shift2Start = shift2.getStartDateTime();
        LocalDateTime shift2End = shift2.getEndDateTime();
        return (int) Duration.between((shift1Start.compareTo(shift2Start) > 0) ? shift1Start : shift2Start,
                (shift1End.compareTo(shift2End) < 0) ? shift1End : shift2End).toMinutes();
    }

    private static int getShiftDurationInMinutes(Shift shift) {
        return (int) Duration.between(shift.getStartDateTime(), shift.getEndDateTime()).toMinutes();
    }

    Constraint requiredDepartment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> !shift.getSeller().getDepartment().equals(shift.getDepartment()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing department");
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, Joiners.equal(Shift::getSeller),
                        Joiners.overlapping(Shift::getStartDateTime, Shift::getEndDateTime))
                .penalize(HardSoftScore.ONE_HARD,
                        EmployeeSchedulingConstraintProvider::getMinuteOverlap)
                .asConstraint("Overlapping shift");
    }

    Constraint atLeast10HoursBetweenTwoShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getSeller),
                        Joiners.lessThanOrEqual(Shift::getEndDateTime, Shift::getStartDateTime))
                .filter((firstShift, secondShift) -> Duration.between(firstShift.getEndDateTime(), secondShift.getStartDateTime()).toHours() < 10)
                .penalize(HardSoftScore.ONE_HARD,
                        (firstShift, secondShift) -> {
                            int breakLength = (int) Duration.between(firstShift.getEndDateTime(), secondShift.getStartDateTime()).toMinutes();
                            return (10 * 60) - breakLength;
                        })
                .asConstraint("At least 10 hours between 2 shifts");
    }

    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, Joiners.equal(Shift::getSeller),
                        Joiners.equal(shift -> shift.getStartDateTime().toLocalDate()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Max one shift per day");
    }

    Constraint unavailableEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(SellerAvailability.class,
                        Joiners.lessThan((Shift shift) -> shift.getStartDateTime(), SellerAvailability::getEndDateTime),
                        Joiners.greaterThan((Shift shift) -> shift.getEndDateTime(), SellerAvailability::getStartDateTime),
                        Joiners.equal(Shift::getSeller, SellerAvailability::getSeller))
                .filter((shift, availability) -> availability.getSellerAvailabilityState() == SellerAvailabilityState.UNAVAILABLE)
                .penalize(HardSoftScore.ONE_HARD,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Unavailable employee");
    }

    Constraint desiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(SellerAvailability.class,
                        Joiners.lessThan((Shift shift) -> shift.getStartDateTime(), SellerAvailability::getEndDateTime),
                        Joiners.greaterThan((Shift shift) -> shift.getEndDateTime(), SellerAvailability::getStartDateTime),
                        Joiners.equal(Shift::getSeller, SellerAvailability::getSeller))
                .filter((shift, availability) -> availability.getSellerAvailabilityState() == SellerAvailabilityState.DESIRED)
                .reward(HardSoftScore.ONE_SOFT,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Desired day for employee");
    }

    Constraint undesiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(SellerAvailability.class,
                        Joiners.lessThan((Shift shift) -> shift.getStartDateTime(), SellerAvailability::getEndDateTime),
                        Joiners.greaterThan((Shift shift) -> shift.getEndDateTime(), SellerAvailability::getStartDateTime),
                        Joiners.equal(Shift::getSeller, SellerAvailability::getSeller))
                .filter((shift, availability) -> availability.getSellerAvailabilityState() == SellerAvailabilityState.UNDESIRED)
                .penalize(HardSoftScore.ONE_SOFT,
                        (shift, availability) -> getShiftDurationInMinutes(shift))
                .asConstraint("Undesired day for employee");
    }

}
