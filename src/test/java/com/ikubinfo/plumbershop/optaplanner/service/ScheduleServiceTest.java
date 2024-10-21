package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.email.service.EmailService;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ScheduleRepository;
import com.ikubinfo.plumbershop.optaplanner.service.impl.ScheduleServiceImpl;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SCHEDULE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@SpringBootTest
@ActiveProfiles("test")
class ScheduleServiceTest {

    @Autowired
    private ScheduleService underTest;
    @Autowired
    private SolverManager<ScheduleDocument, String> solverManager;
    @Mock
    private UserService userService;
    @Mock
    private SellerAvailabilityService sellerAvailabilityService;
    @Mock
    private ShiftService shiftService;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private EmailService emailService;

    @Value("${documents.folder}")
    private String documentPath;

    @BeforeEach
    void setUp() {
        underTest = new ScheduleServiceImpl( solverManager, userService, sellerAvailabilityService,
                shiftService, scheduleRepository, emailService);
    }

    @Test
    void solve_unavailableEmployee() {
        int numberOfDays = 2;
        List<ShiftDocument> shifts = createShiftList();

        LocalDateTime date = LocalDateTime.now();
        UserDocument seller1 = createUserDocument("1", Role.SELLER, "unavailable@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller2 = createUserDocument("2", Role.SELLER, "seller2@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller3 = createUserDocument("3", Role.SELLER, "seller3@gmail.com", Department.DEPARTMENT_TWO);
        UserDocument seller4 = createUserDocument("4", Role.SELLER, "seller4@gmail.com", Department.DEPARTMENT_TWO);
        List<UserDocument> sellers = Arrays.asList(seller1, seller2, seller3, seller4);

        SellerAvailabilityDocument availability1 = createAvailabilityDocument("1",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(20,0)).plusDays(1),
                SellerAvailabilityState.UNAVAILABLE, seller1);
        List<SellerAvailabilityDocument> availabilities = Arrays.asList(availability1);

        ScheduleDocument scheduleDocument = createSchedule();
        scheduleDocument.setEmployeeList(sellers);

        ReflectionTestUtils.setField(underTest, "documentPath", documentPath);
        given(shiftService.createShiftList(numberOfDays)).willReturn(shifts);
        given(userService.getAllUsersBasedOnRole(Role.SELLER)).willReturn(sellers);
        given(sellerAvailabilityService.findAllByDates(any(),any())).willReturn(availabilities);

        ScheduleDto result = underTest.solve(numberOfDays);

        assertThat(result.getShiftList().get(0).getSeller().getEmail()).isNotEqualTo(availability1.getSeller().getEmail());
        assertThat(result.getShiftList().get(1).getSeller().getEmail()).isNotEqualTo(availability1.getSeller().getEmail());

        verify(shiftService).deleteAll();
        verify(emailService).sendScheduleToEmail(any(), any(), any());

    }

    @Test
    void solve_oneShiftPerDay_desired_undesiredShift() {
        LocalDateTime date = LocalDateTime.now();
        int numberOfDays = 2;
        List<ShiftDocument> shifts = createShiftList();

        UserDocument seller1 = createUserDocument("1", Role.SELLER, "seller1@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller2 = createUserDocument("2", Role.SELLER, "seller2@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller3 = createUserDocument("3", Role.SELLER, "seller3@gmail.com", Department.DEPARTMENT_TWO);
        UserDocument seller4 = createUserDocument("4", Role.SELLER, "seller4@gmail.com", Department.DEPARTMENT_TWO);
        List<UserDocument> sellers = Arrays.asList(seller1, seller2, seller3, seller4);

        SellerAvailabilityDocument availability1 = createAvailabilityDocument("1",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(20,0)).plusDays(1),
                SellerAvailabilityState.DESIRED, seller1);
        SellerAvailabilityDocument availability2 = createAvailabilityDocument("2",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(14,0)).plusDays(1),
                SellerAvailabilityState.UNDESIRED, seller2);
        SellerAvailabilityDocument availability3 = createAvailabilityDocument("3",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(10,0)).plusDays(1),
                SellerAvailabilityState.UNDESIRED, seller3);
        SellerAvailabilityDocument availability4 = createAvailabilityDocument("4",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(14,0)).plusDays(1),
                SellerAvailabilityState.UNDESIRED, seller4);
        List<SellerAvailabilityDocument> availabilities = Arrays.asList(availability1,availability2,availability3,availability4);

        ReflectionTestUtils.setField(underTest, "documentPath", documentPath);
        given(shiftService.createShiftList(numberOfDays)).willReturn(shifts);
        given(userService.getAllUsersBasedOnRole(Role.SELLER)).willReturn(sellers);
        given(sellerAvailabilityService.findAllByDates(any(),any())).willReturn(availabilities);

        ScheduleDto result = underTest.solve(numberOfDays);

        assertThat(result.getShiftList().get(0).getSeller().getEmail()).isEqualTo(seller1.getEmail());
        assertThat(result.getShiftList().get(1).getSeller().getEmail()).isEqualTo(seller2.getEmail());
        assertThat(result.getShiftList().get(2).getSeller().getEmail()).isEqualTo(seller3.getEmail());

        verify(shiftService).deleteAll();
        verify(emailService).sendScheduleToEmail(any(), any(), any());

    }

    @Test
    void getAll_success() {
        ScheduleDocument schedule = createSchedule();
        PageParams pageParams = new PageParams();
        Page<ScheduleDocument> mockedPage = new PageImpl<>(List.of(schedule));

        given(scheduleRepository.findAll(any(Pageable.class)))
                .willReturn(mockedPage);

        Page<ScheduleDto> result = underTest.findAll(pageParams);

        assertThat(result.getContent().get(0).getId()).isEqualTo(schedule.getId());

    }

    @Test
    void getById_success() {
        ScheduleDocument schedule = createSchedule();

        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));

        ScheduleDto result = underTest.getById(schedule.getId());

        assertThat(result.getId()).isEqualTo(schedule.getId());
    }

    @Test
    void getById_throwException_notFound() {
        String id = "1";
        given(scheduleRepository.findById(id)).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",SCHEDULE,ID, id);

    }

    @Test
    void deleteById_success() {
        String id = "1";
        String result = underTest.deleteById(id);

        verify(scheduleRepository).deleteById(id);

        assertThat(result).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT,SCHEDULE));
    }

    private ScheduleDocument createSchedule(){

        List<ShiftDocument> shifts = createShiftList();

        UserDocument seller1 = createUserDocument("1", Role.SELLER, "seller1@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller2 = createUserDocument("2", Role.SELLER, "seller2@gmail.com", Department.DEPARTMENT_ONE);
        UserDocument seller3 = createUserDocument("3", Role.SELLER, "seller3@gmail.com", Department.DEPARTMENT_TWO);
        UserDocument seller4 = createUserDocument("4", Role.SELLER, "seller4@gmail.com", Department.DEPARTMENT_TWO);
        List<UserDocument> sellers = Arrays.asList(seller1, seller2, seller3, seller4);

        LocalDateTime date = LocalDateTime.now();
        SellerAvailabilityDocument availability1 = createAvailabilityDocument("1",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(20,0)).plusDays(1),
                SellerAvailabilityState.DESIRED, seller1);
        SellerAvailabilityDocument availability2 = createAvailabilityDocument("2",
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(14,0)).plusDays(1),
                SellerAvailabilityState.UNDESIRED, seller2);
        List<SellerAvailabilityDocument> availabilities = Arrays.asList(availability1,availability2);

        ScheduleDocument scheduleDocument = new ScheduleDocument();
        scheduleDocument.setId("1");
        scheduleDocument.setShiftList(shifts);
        scheduleDocument.setAvailabilityList(availabilities);
        scheduleDocument.setEmployeeList(sellers);

        return scheduleDocument;
    }

    private ShiftDocument createShift(String id, Department department, LocalDateTime startTime, LocalDateTime endTime) {
        ShiftDocument shift = new ShiftDocument();
        shift.setId(id);
        shift.setDepartment(department);
        shift.setStartDateTime(startTime);
        shift.setEndDateTime(endTime);
        return shift;
    }

    private List<ShiftDocument> createShiftList() {
        LocalDateTime date = LocalDateTime.now();
        //first day
        ShiftDocument shift1 = createShift("1", Department.DEPARTMENT_ONE, date.with(LocalTime.of(8,0)).plusDays(1), date.with(LocalTime.of(14,0)).plusDays(1));
        ShiftDocument shift2 = createShift("2", Department.DEPARTMENT_ONE, date.with(LocalTime.of(14,0)).plusDays(1), date.with(LocalTime.of(20,0)).plusDays(1));
        ShiftDocument shift3 = createShift("3", Department.DEPARTMENT_TWO, date.with(LocalTime.of(8,0)).plusDays(1), date.with(LocalTime.of(14,0)).plusDays(1));
        ShiftDocument shift4 = createShift("4", Department.DEPARTMENT_TWO, date.with(LocalTime.of(14,0)).plusDays(1), date.with(LocalTime.of(20,0)).plusDays(1));
        //second day
        ShiftDocument shift5 = createShift("5", Department.DEPARTMENT_ONE, date.with(LocalTime.of(8,0)).plusDays(2), date.with(LocalTime.of(14,0)).plusDays(2));
        ShiftDocument shift6 = createShift("6", Department.DEPARTMENT_ONE, date.with(LocalTime.of(14,0)).plusDays(2), date.with(LocalTime.of(20,0)).plusDays(2));
        ShiftDocument shift7 = createShift("7", Department.DEPARTMENT_TWO, date.with(LocalTime.of(8,0)).plusDays(2), date.with(LocalTime.of(14,0)).plusDays(2));
        ShiftDocument shift8 = createShift("8", Department.DEPARTMENT_TWO, date.with(LocalTime.of(14,0)).plusDays(2), date.with(LocalTime.of(20,0)).plusDays(2));

        List<ShiftDocument> shifts = Arrays.asList(shift1, shift2, shift3, shift4, shift5, shift6, shift7, shift8);
        return shifts;
    }

    private SellerAvailabilityDocument createAvailabilityDocument(String id, LocalDateTime start,
                                                                  LocalDateTime end,
                                                                  SellerAvailabilityState state,
                                                                  UserDocument seller) {
        SellerAvailabilityDocument document = new SellerAvailabilityDocument();
        document.setId(id);
        document.setStartDateTime(start);
        document.setEndDateTime(end);
        document.setSellerAvailabilityState(state);
        document.setSeller(seller);

        return document;
    }

    private UserDocument createUserDocument(String id, Role role, String email, Department department) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(id);
        userDocument.setFirstName("Artjola1");
        userDocument.setLastName("Kotorri1");
        userDocument.setEmail(email);
        userDocument.setRole(role);
        userDocument.setPassword("1A@a2345678");
        userDocument.setDepartment(department);
        return userDocument;
    }

}