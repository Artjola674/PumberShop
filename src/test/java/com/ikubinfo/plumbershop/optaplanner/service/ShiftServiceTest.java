package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ShiftRepository;
import com.ikubinfo.plumbershop.optaplanner.service.impl.ShiftServiceImpl;
import com.ikubinfo.plumbershop.user.enums.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class ShiftServiceTest {

    @Autowired
    private ShiftService underTest;

    @Mock
    private ShiftRepository shiftRepository;

    @Value("${first-shift-start-time}")
    public int firstShiftStartHour;
    @Value("${first-shift-end-time}")
    private int firstShiftEndHour;
    @Value("${second-shift-start-time}")
    private int secondShiftStartHour;
    @Value("${second-shift-end-time}")
    private int secondShiftEndHour;

    @BeforeEach
    void setUp() {
        underTest = new ShiftServiceImpl(shiftRepository);
    }

    @Test
    void createShiftList_success() {

        List<ShiftDocument> shifts = createShiftList();

        ReflectionTestUtils.setField(underTest, "firstShiftStartHour", firstShiftStartHour);
        ReflectionTestUtils.setField(underTest, "firstShiftEndHour", firstShiftEndHour);
        ReflectionTestUtils.setField(underTest, "secondShiftStartHour", secondShiftStartHour);
        ReflectionTestUtils.setField(underTest, "secondShiftEndHour", secondShiftEndHour);

        given(shiftRepository.saveAll(shifts)).willReturn(shifts);

        List<ShiftDocument> result = underTest.createShiftList(2);

        assertThat(result.size()).isEqualTo(shifts.size());
        assertThat(result.get(0)).isEqualTo(shifts.get(0));
    }

    @Test
    void deleteAll_success() {

        underTest.deleteAll();

        verify(shiftRepository).deleteAll();
    }

    private ShiftDocument createShift(Department department, LocalDateTime startTime, LocalDateTime endTime) {
        ShiftDocument shift = new ShiftDocument();
        shift.setDepartment(department);
        shift.setStartDateTime(startTime);
        shift.setEndDateTime(endTime);
        return shift;
    }

    private List<ShiftDocument> createShiftList() {
        LocalDateTime date = LocalDateTime.now();
        //first day
        ShiftDocument shift1 = createShift(Department.DEPARTMENT_ONE, date.with(LocalTime.of(8,0)).plusDays(1), date.with(LocalTime.of(14,0)).plusDays(1));
        ShiftDocument shift2 = createShift(Department.DEPARTMENT_ONE, date.with(LocalTime.of(14,0)).plusDays(1), date.with(LocalTime.of(20,0)).plusDays(1));
        ShiftDocument shift3 = createShift(Department.DEPARTMENT_TWO, date.with(LocalTime.of(8,0)).plusDays(1), date.with(LocalTime.of(14,0)).plusDays(1));
        ShiftDocument shift4 = createShift(Department.DEPARTMENT_TWO, date.with(LocalTime.of(14,0)).plusDays(1), date.with(LocalTime.of(20,0)).plusDays(1));
        //second day
        ShiftDocument shift5 = createShift(Department.DEPARTMENT_ONE, date.with(LocalTime.of(8,0)).plusDays(2), date.with(LocalTime.of(14,0)).plusDays(2));
        ShiftDocument shift6 = createShift(Department.DEPARTMENT_ONE, date.with(LocalTime.of(14,0)).plusDays(2), date.with(LocalTime.of(20,0)).plusDays(2));
        ShiftDocument shift7 = createShift(Department.DEPARTMENT_TWO, date.with(LocalTime.of(8,0)).plusDays(2), date.with(LocalTime.of(14,0)).plusDays(2));
        ShiftDocument shift8 = createShift(Department.DEPARTMENT_TWO, date.with(LocalTime.of(14,0)).plusDays(2), date.with(LocalTime.of(20,0)).plusDays(2));

        List<ShiftDocument> shifts = Arrays.asList(shift1, shift2, shift3, shift4, shift5, shift6, shift7, shift8);
        return shifts;
    }
}