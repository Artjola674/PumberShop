package com.ikubinfo.plumbershop.optaplanner.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.CustomPageImpl;
import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.optaplanner.dto.SellerAvailabilityDto;
import com.ikubinfo.plumbershop.optaplanner.enums.SellerAvailabilityState;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.SellerAvailabilityRepository;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.enums.Role;
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

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SELLER_AVAILABILITY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SellerAvailabilityControllerTest extends BaseTest {

    private static final String AVAILABILITY_URL = BASE_URL + "/availability";

    private final SellerAvailabilityRepository availabilityRepository;

    @Autowired
    public SellerAvailabilityControllerTest(SellerAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @AfterEach
    void tearDown() {
        deleteUsers();
        availabilityRepository.deleteAll();
    }

    @Test
    void create_pass() {
        SellerAvailabilityDto availabilityDto = createAvailabilityDto("DESIRED");

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<SellerAvailabilityDto> entity = new HttpEntity<>(availabilityDto, headers);

        ResponseEntity<SellerAvailabilityDto> response = restTemplate.postForEntity(AVAILABILITY_URL,
                entity, SellerAvailabilityDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(availabilityDto.getSeller().getEmail());
    }

    @Test
    void create_fail_invalidStateValue() {
        try {
            SellerAvailabilityDto availabilityDto = createAvailabilityDto("DESIREDD");

            HttpHeaders headers = createHeaders(getTokenForAdmin());

            HttpEntity<SellerAvailabilityDto> entity = new HttpEntity<>(availabilityDto, headers);

            restTemplate.postForEntity(AVAILABILITY_URL,
                    entity, SellerAvailabilityDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains("Availability state must be 'DESIRED', 'UNDESIRED' or 'UNAVAILABLE' ");
        }
    }

    @Test
    void getAllAvailabilities_pass() {

        SellerAvailabilityDocument availability1 = createAvailabilityDocument();
        availabilityRepository.save(availability1);
        SellerAvailabilityDocument availability2 = createAvailabilityDocument();
        availabilityRepository.save(availability2);
        SellerAvailabilityDocument availability3 = createAvailabilityDocument();
        SellerAvailabilityDocument saved = availabilityRepository.save(availability3);

        HttpHeaders headers = createHeaders(getTokenForAdmin());
        PageParams pageParams = new PageParams();
        pageParams.setPageSize(2);

        HttpEntity<PageParams> entity = new HttpEntity<>(pageParams, headers);

        ResponseEntity<CustomPageImpl<SellerAvailabilityDto>> response = restTemplate.exchange(AVAILABILITY_URL,
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);

        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(saved.getId());

    }

    @Test
    void getAllAvailabilities_fail_invalidSortType() {
        try {
            SellerAvailabilityDocument availability1 = createAvailabilityDocument();
            availabilityRepository.save(availability1);

            HttpHeaders headers = createHeaders(getTokenForAdmin());
            PageParams pageParams = new PageParams();
            pageParams.setPageSize(2);
            pageParams.setSortType("DESSC");

            HttpEntity<PageParams> entity = new HttpEntity<>(pageParams, headers);

            restTemplate.exchange(AVAILABILITY_URL,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains("Sort type must be 'DESC' or 'ASC'");
        }
    }

    @Test
    void getAvailabilityById_pass() {
        SellerAvailabilityDocument availability = createAvailabilityDocument();
        SellerAvailabilityDocument saved = availabilityRepository.save(availability);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<SellerAvailabilityDto> response = restTemplate.exchange(
                AVAILABILITY_URL+"/id/" + saved.getId(), HttpMethod.GET,
                new HttpEntity<>( headers), SellerAvailabilityDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSellerAvailabilityState()).isEqualTo(saved.getSellerAvailabilityState().toString());

    }

    @Test
    void getAvailabilityById_fail_notFound() {
        try {

            HttpHeaders headers = createHeaders(getTokenForAdmin());

            restTemplate.exchange(
                    AVAILABILITY_URL+"/id/" + UtilClass.createRandomString(), HttpMethod.GET,
                    new HttpEntity<>( headers), SellerAvailabilityDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Test
    void updateAvailabilityById_pass() {
        SellerAvailabilityDocument availability = createAvailabilityDocument();
        SellerAvailabilityDocument saved = availabilityRepository.save(availability);

        SellerAvailabilityDto availabilityDto = createAvailabilityDto("UNAVAILABLE");

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<SellerAvailabilityDto> entity = new HttpEntity<>(availabilityDto, headers);

        ResponseEntity<SellerAvailabilityDto> response = restTemplate.exchange(
                AVAILABILITY_URL+"/id/"+saved.getId(), HttpMethod.PUT,
                entity, SellerAvailabilityDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSellerAvailabilityState()).isEqualTo(availabilityDto.getSellerAvailabilityState().toString());
        assertThat(response.getBody().getSellerAvailabilityState()).isNotEqualTo(saved.getSellerAvailabilityState().toString());
    }

    @Test
    void updateAvailabilityById_fail_nullSeller() {
        try {
            SellerAvailabilityDocument availability = createAvailabilityDocument();
            SellerAvailabilityDocument saved = availabilityRepository.save(availability);

            SellerAvailabilityDto availabilityDto = createAvailabilityDto("UNAVAILABLE");
            availabilityDto.setSeller(null);

            HttpHeaders headers = createHeaders(getTokenForAdmin());

            HttpEntity<SellerAvailabilityDto> entity = new HttpEntity<>(availabilityDto, headers);

            restTemplate.exchange(AVAILABILITY_URL+"/id/"+ saved.getId(), HttpMethod.PUT,
                    entity, SellerAvailabilityDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(INPUT_NOT_NULL);
        }

    }

    @Test
    void deleteAvailabilityById_pass() {
        SellerAvailabilityDocument availability = createAvailabilityDocument();
        SellerAvailabilityDocument saved = availabilityRepository.save(availability);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<String> response = restTemplate.exchange(
                AVAILABILITY_URL+"/id/" + saved.getId(), HttpMethod.DELETE,
                new HttpEntity<>( headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT, SELLER_AVAILABILITY));

    }

    @Test
    void deleteAvailabilityById_fail_notAdmin() {
        try {

            SellerAvailabilityDocument availability = createAvailabilityDocument();
            SellerAvailabilityDocument saved = availabilityRepository.save(availability);

            HttpHeaders headers = createHeaders(getTokenForPlumber());

            restTemplate.exchange(AVAILABILITY_URL+"/id/" + saved.getId(), HttpMethod.DELETE,
                    new HttpEntity<>( headers), String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("Access Denied");
        }

    }

    private SellerAvailabilityDocument createAvailabilityDocument() {
        SellerAvailabilityDocument document = new SellerAvailabilityDocument();
        document.setStartDateTime(LocalDateTime.now());
        document.setEndDateTime(LocalDateTime.now());
        document.setSellerAvailabilityState(SellerAvailabilityState.UNDESIRED);

        return document;
    }

    private SellerAvailabilityDto createAvailabilityDto(String state) {
        SellerAvailabilityDto dto = new SellerAvailabilityDto();
        dto.setId("1");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now());
        dto.setSellerAvailabilityState(state);
        dto.setSeller(createUserDto());

        return dto;
    }

    private UserDto createUserDto() {
        UserDto userDto = new UserDto();
        userDto.setFirstName("Artjola");
        userDto.setLastName("Kotorri");
        userDto.setEmail("artjolakotorri@gmail.com");
        userDto.setRole(String.valueOf(Role.PLUMBER));
        userDto.setPassword("A@a2345678");
        userDto.setPhone("0681456789");
        return userDto;
    }
}