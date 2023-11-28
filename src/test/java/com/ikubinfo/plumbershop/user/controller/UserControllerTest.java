package com.ikubinfo.plumbershop.user.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.CustomPageImpl;
import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.user.dto.*;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.ResetTokenRepository;
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

import java.util.Date;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.*;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class UserControllerTest extends BaseTest {

    private static final String USER_URL = BASE_URL + "/users";

    private final UserRepository userRepository;

    private final ResetTokenRepository resetTokenRepository;

    @Autowired
    public UserControllerTest(UserRepository userRepository, ResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
    }


    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        resetTokenRepository.deleteAll();
    }

    @Test
    void saveUser_pass_loggedUserAdmin() {
        UserDto userDto = createUserDto();

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = restTemplate.postForEntity(USER_URL,
                entity, UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    void saveUser_pass_selfRegister() {

        UserDto userDto = createUserDto();
        userDto.setRole(Role.ADMIN.toString());

        HttpEntity<UserDto> entity = new HttpEntity<>(userDto);

        ResponseEntity<UserDto> response = restTemplate.postForEntity(USER_URL, entity, UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo(userDto.getEmail());
        assertThat(response.getBody().getRole()).isEqualTo(Role.USER.toString());
        assertThat(response.getBody().getRole()).isNotEqualTo(userDto.getRole().toString());
    }

    @Test
    void saveUser_fail_notAdmin_notSelfRegister() {
        try {
            UserDto userDto = createUserDto();

            HttpHeaders headers = createHeaders(getTokenForSeller());

            HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

            restTemplate.postForEntity(USER_URL, entity, UserDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(ACTION_NOT_ALLOWED);
        }
    }

    @Test
    void saveUser_fail_emailExists() {
        UserDto userDto = createUserDto();
        try {

            HttpEntity<UserDto> entity = new HttpEntity<>(userDto);

            restTemplate.postForEntity(USER_URL, entity, UserDto.class);
            restTemplate.postForEntity(USER_URL, entity, UserDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(EMAIL_EXISTS + userDto.getEmail());
        }

    }


    @Test
    void getAllUsers_withFiler() {
        UserDocument user1 = createUserDocument("user1@gmail.com", Role.SELLER);
        userRepository.save(user1);
        UserDocument user2 = createUserDocument("user2@gmail.com", Role.USER);
        userRepository.save(user2);
        UserDocument user3 = createUserDocument("user3@gmail.com", Role.SELLER);
        userRepository.save(user3);

        UserRequest userRequest = new UserRequest();
        userRequest.setPageParams(new PageParams());
        userRequest.setRole(user1.getRole().toString());

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        HttpEntity<UserRequest> entity = new HttpEntity<>(userRequest, headers);

        ResponseEntity<CustomPageImpl<UserDto>> response = restTemplate.exchange(USER_URL,
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().get().findAny().get().getRole()).isEqualTo(user1.getRole().toString());
        assertThat(response.getBody().get().findAny().get().getRole()).isNotEqualTo(user2.getRole().toString());

    }

    @Test
    void getAllUsers_withoutFiler() {
        UserDocument user1 = createUserDocument("user1@gmail.com", Role.SELLER);
        userRepository.save(user1);
        UserDocument user2 = createUserDocument("user2@gmail.com", Role.USER);
        userRepository.save(user2);
        UserDocument user3 = createUserDocument("user3@gmail.com", Role.SELLER);
        userRepository.save(user3);

        UserRequest userRequest = new UserRequest();
        PageParams pageParams = new PageParams();
        pageParams.setPageSize(2);
        userRequest.setPageParams(pageParams);

        HttpHeaders headers = createHeaders(getTokenForAdmin()); //creates new user

        HttpEntity<UserRequest> entity = new HttpEntity<>(userRequest, headers);

        ResponseEntity<CustomPageImpl<UserDto>> response = restTemplate.exchange(USER_URL,
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(4);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
    }

    @Test
    void getUserById_pass_admin() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<UserDto> response = restTemplate.exchange(
                USER_URL+"/id/"+savedUser.getId(), HttpMethod.GET,
                new HttpEntity<>( headers), UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void getUserById_fail_notFound() {
        try {
            HttpHeaders headers = createHeaders(getTokenForAdmin());

            restTemplate.exchange(
                    USER_URL+"/id/"+ UtilClass.createRandomString(), HttpMethod.GET,
                    new HttpEntity<>( headers), UserDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void getUserById_pass_notAdmin() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);

        HttpHeaders headers = createHeaders(doLogin(user.getEmail()).getAccessToken());

        ResponseEntity<UserDto> response = restTemplate.exchange(
                USER_URL+"/id/"+savedUser.getId(), HttpMethod.GET,
                new HttpEntity<>( headers), UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void getUserById_fail_notAdmin() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);

            HttpHeaders headers = createHeaders(getTokenForUser());

            restTemplate.exchange(
                    USER_URL+"/id/"+savedUser.getId(), HttpMethod.GET,
                    new HttpEntity<>( headers), UserDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(ACTION_NOT_ALLOWED);
        }

    }

    @Test
    void updateUserById_pass() {

        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);

        UserDto userDto = createUserDto();

        HttpHeaders headers = createHeaders(getTokenForAdmin());
        HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                USER_URL+"/id/"+savedUser.getId(), HttpMethod.PUT,
                entity, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getRole()).isEqualTo(savedUser.getRole().toString());
        assertThat(response.getBody().getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    void updateUserById_fail_emailExists() {

        try {

            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);

            UserDocument user2 = createUserDocument("user2@gmail.com", Role.SELLER);
            userRepository.save(user2);

            UserDto userDto = createUserDto();
            userDto.setEmail(user2.getEmail());

            HttpHeaders headers = createHeaders(getTokenForAdmin());
            HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

            restTemplate.exchange(
                    USER_URL+"/id/"+savedUser.getId(), HttpMethod.PUT,
                    entity, UserDto.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(EMAIL_EXISTS);
        }
    }

    @Test
    void deleteUserById_pass() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);

        HttpHeaders headers = createHeaders(getTokenForAdmin());

        ResponseEntity<String> response = restTemplate.exchange(
                USER_URL + "/id/" + savedUser.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT, USER));
    }

    @Test
    void deleteUserById_fail_notAdmin() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);

            HttpHeaders headers = createHeaders(getTokenForSeller());

            restTemplate.exchange(
                    USER_URL + "/id/" + savedUser.getId(), HttpMethod.DELETE,
                    new HttpEntity<>(headers), String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("Access Denied");
        }
    }

    @Test
    void changePassword_pass() {

        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);
        ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, "NewPass@!1245");


        HttpHeaders headers = createHeaders(doLogin(user.getEmail()).getAccessToken());
        HttpEntity<ChangePasswordDto> entity = new HttpEntity<>(changePasswordDto, headers);


        ResponseEntity<String> response = restTemplate.exchange(
                USER_URL+"/changePassword/id/"+savedUser.getId(), HttpMethod.PATCH,
                entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(PASS_CHANGED_SUCCESSFULLY);
    }

    @Test
    void changePassword_fail_passNotMatch() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);
            ChangePasswordDto changePasswordDto = new ChangePasswordDto("2Password@123", "1Password@123");


            HttpHeaders headers = createHeaders(doLogin(user.getEmail()).getAccessToken());
            HttpEntity<ChangePasswordDto> entity = new HttpEntity<>(changePasswordDto, headers);

            restTemplate.exchange(
                    USER_URL+"/changePassword/id/"+savedUser.getId(), HttpMethod.PATCH,
                    entity, String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(PASS_NOT_CORRECT);
        }

    }

    @Test
    void changePassword_fail_invalidPass() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);
            ChangePasswordDto changePasswordDto = new ChangePasswordDto(PASSWORD, "newPass");


            HttpHeaders headers = createHeaders(doLogin(user.getEmail()).getAccessToken());
            HttpEntity<ChangePasswordDto> entity = new HttpEntity<>(changePasswordDto, headers);

            restTemplate.exchange(
                    USER_URL+"/changePassword/id/"+savedUser.getId(), HttpMethod.PATCH,
                    entity, String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getMessage()).contains(PASS_VALIDATE_MESSAGE);
        }
    }


    @Test
    void forgetPassword_pass() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        userRepository.save(user);

        ResponseEntity<String> response = restTemplate.exchange(
                USER_URL + "/forgetPassword?email=" + user.getEmail(), HttpMethod.PATCH,
                null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(FORGET_PASS);
    }

    @Test
    void forgetPassword_fail_userNotFound() {
        try {
            String email = "user1@gmail.com";

            restTemplate.exchange(
                    USER_URL + "/forgetPassword?email=" + email, HttpMethod.PATCH,
                    null, String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Test
    void resetPassword_pass() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);
        ResetTokenDocument resetTokenDocument = createResetToken(savedUser,new Date(System.currentTimeMillis() + 50000));
        resetTokenRepository.save(resetTokenDocument);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto(PASSWORD,PASSWORD);
        HttpEntity<ResetPasswordDto> entity = new HttpEntity<>(resetPasswordDto);

        ResponseEntity<String> response = restTemplate.exchange(
                USER_URL + "/resetPassword?ticket=" + resetTokenDocument.getToken(),
                HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(PASS_CHANGED_SUCCESSFULLY);

    }

    @Test
    void resetPassword_fail_expiredToken() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);
            ResetTokenDocument resetTokenDocument = createResetToken(savedUser,new Date(System.currentTimeMillis() - 50000));
            resetTokenRepository.save(resetTokenDocument);

            ResetPasswordDto resetPasswordDto = new ResetPasswordDto(PASSWORD,PASSWORD);
            HttpEntity<ResetPasswordDto> entity = new HttpEntity<>(resetPasswordDto);

            restTemplate.exchange(
                    USER_URL + "/resetPassword?ticket=" + resetTokenDocument.getToken(),
                    HttpMethod.PATCH, entity, String.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }  catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(e.getMessage()).contains(RESET_TOKEN_EXPIRED);
        }

    }


    private UserDto createUserDto() {
        UserDto userDto = new UserDto();
        userDto.setFirstName("Artjola");
        userDto.setLastName("Kotorri");
        userDto.setEmail("artjolakotorri@gmail.com");
        userDto.setRole(String.valueOf(Role.PLUMBER));
        userDto.setPassword("A@a2345678");
        userDto.setAddress(createAddress());
        userDto.setPhone("0681456789");
        return userDto;
    }

    private Address createAddress(){
        Address address = new Address();
        address.setCity("city");
        address.setStreet("street");
        address.setPostalCode(1001);
        return address;
    }

    private ResetTokenDocument createResetToken(UserDocument user, Date date) {
        return ResetTokenDocument.builder()
                .token("123456789")
                .expirationDate(date)
                .user(user)
                .build();
    }
}