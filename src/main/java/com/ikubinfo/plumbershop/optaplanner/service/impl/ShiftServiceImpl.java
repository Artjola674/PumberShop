package com.ikubinfo.plumbershop.optaplanner.service.impl;

import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ShiftRepository;
import com.ikubinfo.plumbershop.optaplanner.service.ShiftService;
import com.ikubinfo.plumbershop.user.enums.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    @Value("${first-shift-start-time}")
    private int firstShiftStartHour;
    @Value("${first-shift-end-time}")
    private int firstShiftEndHour;
    @Value("${second-shift-start-time}")
    private int secondShiftStartHour;
    @Value("${second-shift-end-time}")
    private int secondShiftEndHour;

    // Get the current date


    @Override
    public List<ShiftDocument> createShiftList(int daysAfterToday) {
        List<ShiftDocument> shifts = IntStream.range(0, daysAfterToday)
                .mapToObj(this::createShiftsForDay)
                .flatMap(List::stream)
                .toList();
        shiftRepository.saveAll(shifts);
        return shifts;
    }

    @Override
    public void deleteAll() {
        shiftRepository.deleteAll();
    }

    private ShiftDocument createShift(Department department, LocalDateTime startTime, LocalDateTime endTime) {
        ShiftDocument shift = new ShiftDocument();
        shift.setDepartment(department);
        shift.setStartDateTime(startTime);
        shift.setEndDateTime(endTime);
        return shift;
    }

    private List<ShiftDocument> createShiftsForDay(int daysAfterToday){
        List<ShiftDocument> shifts = new ArrayList<>();
        for (Department department : Department.values()){
            ShiftDocument firstShift = createShift(department, createLocalDateTime(firstShiftStartHour, daysAfterToday),
                    createLocalDateTime(firstShiftEndHour, daysAfterToday));
            ShiftDocument secondShift = createShift(department, createLocalDateTime(secondShiftStartHour, daysAfterToday),
                    createLocalDateTime(secondShiftEndHour, daysAfterToday));
            shifts.add(firstShift);
            shifts.add(secondShift);
        }
        return shifts;
    }

    private LocalDateTime createLocalDateTime(int hour,int daysToAdd){
        LocalDateTime currentDateTime = LocalDateTime.now();
        currentDateTime = currentDateTime.with(LocalTime.of(hour, 0));
        return currentDateTime.plusDays(daysToAdd);
    }
}
