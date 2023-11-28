package com.ikubinfo.plumbershop.optaplanner.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.CustomPageImpl;
import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.dto.ShiftDto;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ScheduleRepository;
import com.ikubinfo.plumbershop.optaplanner.repo.SellerAvailabilityRepository;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.ikubinfo.plumbershop.common.constants.Constants.DELETED_SUCCESSFULLY;
import static com.ikubinfo.plumbershop.common.constants.Constants.DOCUMENT;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SCHEDULE;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SELLER_AVAILABILITY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ScheduleControllerTest extends BaseTest {

    private static final String SCHEDULE_URL = BASE_URL + "/schedule";

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SellerAvailabilityRepository availabilityRepository;

    @Autowired
    public ScheduleControllerTest(ScheduleRepository scheduleRepository, UserRepository userRepository, SellerAvailabilityRepository availabilityRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
        availabilityRepository.deleteAll();
    }

    @Test
    void solve_requiredDepartment() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<ScheduleDto> response = restTemplate.postForEntity(
                SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class);

        ShiftDto shift = response.getBody().getShiftList().stream().findAny().get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shift.getDepartment()).isEqualTo(shift.getSeller().getDepartment());

    }

    @Test
    void solve_unavailableEmployee() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<ScheduleDto> response = restTemplate.postForEntity(
                SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getShiftList().get(2).getSeller().getEmail()).isNotEqualTo("user3@gmail.com");
        assertThat(response.getBody().getShiftList().get(3).getSeller().getEmail()).isNotEqualTo("user3@gmail.com");
        assertThat(response.getBody().getShiftList().get(6).getSeller().getEmail()).isNotEqualTo("user4@gmail.com");

    }

    @Test
    void solve_desiredShift() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<ScheduleDto> response = restTemplate.postForEntity(
                SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getShiftList().get(0).getSeller().getEmail()).isEqualTo("user1@gmail.com");
        assertThat(response.getBody().getShiftList().get(1).getSeller().getEmail()).isEqualTo("user2@gmail.com");

    }

    @Test
    void solve_undesiredShift() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<ScheduleDto> response = restTemplate.postForEntity(
                SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getShiftList().get(4).getSeller().getEmail()).isNotEqualTo("user1@gmail.com");
        assertThat(response.getBody().getShiftList().get(5).getSeller().getEmail()).isNotEqualTo("user2@gmail.com");

    }

    @Test
    void getAll_pass() {

        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3", new HttpEntity<>(headers), ScheduleDto.class);
        restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3", new HttpEntity<>(headers), ScheduleDto.class);

        PageParams pageParams = new PageParams();
        pageParams.setPageSize(1);

        HttpEntity<PageParams> entity = new HttpEntity<>(pageParams, headers);

        ResponseEntity<CustomPageImpl<SellerAvailabilityDto>> response = restTemplate.exchange(SCHEDULE_URL,
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
    }

    @Test
    void getAll_fail_invalidSortType() {
        try {
            createPrerequisites();

            HttpHeaders headers = createHeaders(getTokenForAdmin());

            restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3", new HttpEntity<>(headers), ScheduleDto.class);
            restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3", new HttpEntity<>(headers), ScheduleDto.class);

            PageParams pageParams = new PageParams();
            pageParams.setSortType("DESSC");

            HttpEntity<PageParams> entity = new HttpEntity<>(pageParams, headers);

            restTemplate.exchange(SCHEDULE_URL,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains("Sort type must be 'DESC' or 'ASC'");
        }
    }

    @Test
    void getScheduleById_pass() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ScheduleDto schedule = restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class).getBody();


        ResponseEntity<SellerAvailabilityDto> response = restTemplate.exchange(
                SCHEDULE_URL+"/id/" + schedule.getId(), HttpMethod.GET,
                new HttpEntity<>( headers), SellerAvailabilityDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(schedule.getId());

    }

    @Test
    void getScheduleById_fail_notFound() {
        try {
            HttpHeaders headers = createHeaders(getTokenForAdmin());

            restTemplate.exchange(SCHEDULE_URL+"/id/" + UtilClass.createRandomString(), HttpMethod.GET,
                    new HttpEntity<>( headers), SellerAvailabilityDto.class);
            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Test
    void deleteScheduleById_pass() {
        createPrerequisites();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ScheduleDto schedule = restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3",
                new HttpEntity<>(headers), ScheduleDto.class).getBody();


        ResponseEntity<String> response = restTemplate.exchange(
                SCHEDULE_URL+"/id/" + schedule.getId(), HttpMethod.DELETE,
                new HttpEntity<>( headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT, SCHEDULE));

    }

    @Test
    void deleteScheduleById_fail_notAdmin() {
        try {
            createPrerequisites();

            HttpHeaders headers = createHeaders(getTokenForSeller());

            ScheduleDto schedule = restTemplate.postForEntity(SCHEDULE_URL + "/solve?numberOfDays=3",
                    new HttpEntity<>(headers), ScheduleDto.class).getBody();


            restTemplate.exchange(SCHEDULE_URL+"/id/" + schedule.getId(), HttpMethod.DELETE,
                    new HttpEntity<>( headers), SellerAvailabilityDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("Access Denied");
        }
    }

    private void createPrerequisites() {

        UserDocument user1 = createUserDocument("user1@gmail.com", Role.SELLER);
        user1.setDepartment(Department.DEPARTMENT_ONE);
        userRepository.save(user1);
        UserDocument user2 = createUserDocument("user2@gmail.com", Role.SELLER);
        user2.setDepartment(Department.DEPARTMENT_ONE);
        userRepository.save(user2);
        UserDocument user3 = createUserDocument("user3@gmail.com", Role.SELLER);
        user3.setDepartment(Department.DEPARTMENT_TWO);
        userRepository.save(user3);
        UserDocument user4 = createUserDocument("user4@gmail.com", Role.SELLER);
        user4.setDepartment(Department.DEPARTMENT_TWO);
        userRepository.save(user4);

        LocalDateTime date = LocalDateTime.now();
        //unavailable
        SellerAvailabilityDocument availability1 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(20,0)).plusDays(1),
                SellerAvailabilityState.UNAVAILABLE, user3);
        availabilityRepository.save(availability1);
        SellerAvailabilityDocument availability5 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(20,0)).plusDays(1),
                SellerAvailabilityState.UNDESIRED, user3);
        availabilityRepository.save(availability5);
        SellerAvailabilityDocument availability2 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(2),
                date.with(LocalTime.of(12,0)).plusDays(2),
                SellerAvailabilityState.UNAVAILABLE, user4);
        availabilityRepository.save(availability2);

        //desired
        SellerAvailabilityDocument availability3 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(14,0)).plusDays(1),
                SellerAvailabilityState.DESIRED, user1);
        availabilityRepository.save(availability3);
        SellerAvailabilityDocument availability4 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(1),
                date.with(LocalTime.of(10,0)).plusDays(1),
                SellerAvailabilityState.DESIRED, user2);
        availabilityRepository.save(availability4);

        //undesired
        SellerAvailabilityDocument availability6 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(2),
                date.with(LocalTime.of(14,0)).plusDays(2),
                SellerAvailabilityState.UNDESIRED, user1);
        availabilityRepository.save(availability6);
        SellerAvailabilityDocument availability7 = createAvailabilityDocument(
                date.with(LocalTime.of(8,0)).plusDays(2),
                date.with(LocalTime.of(10,0)).plusDays(2),
                SellerAvailabilityState.UNDESIRED, user2);
        availabilityRepository.save(availability7);
    }


    private SellerAvailabilityDocument createAvailabilityDocument(LocalDateTime start, LocalDateTime end,
                                                                  SellerAvailabilityState state, UserDocument seller) {
        SellerAvailabilityDocument document = new SellerAvailabilityDocument();
        document.setStartDateTime(start);
        document.setEndDateTime(end);
        document.setSellerAvailabilityState(state);
        document.setSeller(seller);

        return document;
    }
}