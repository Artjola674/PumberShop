package com.ikubinfo.plumbershop.auth.controller;

import com.ikubinfo.plumbershop.BaseTest;
import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.auth.dto.TokenRefreshRequest;
import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.user.dto.ResetPasswordDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Date;

import static com.ikubinfo.plumbershop.auth.constants.Constants.REFRESH_TOKEN_EXPIRED;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.RESET_TOKEN_EXPIRED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class AuthControllerTest extends BaseTest {

    private static final String AUTH_URL = BASE_URL + "/auth";

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthControllerTest(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    void authenticate_success() {
        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        userRepository.save(user);

        AuthRequest authRequest = createAuthRequest(user.getEmail(), PASSWORD);

        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                AUTH_URL + "/login", entity, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getRefreshToken()).isNotNull();
    }

    @Test
    void authenticate_fail_wrongPassword() {
        try {

            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            userRepository.save(user);

            AuthRequest authRequest= createAuthRequest(user.getEmail(), "12345678");

            HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest);

            restTemplate.postForEntity(
                    AUTH_URL + "/login", entity, AuthResponse.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("Bad credentials");
        }
    }

    @Test
    void authenticate_fail_wrongEmail() {
        try {
            String email = "user@gmail.com";
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            userRepository.save(user);

            AuthRequest authRequest= createAuthRequest(email, PASSWORD);

            HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest);
            restTemplate.postForEntity(
                    AUTH_URL + "/login", entity, AuthResponse.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        } catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getMessage()).contains("user not found with username");
        }

    }


    @Test
    void generateTokenFromRefreshToken_success() {

        UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
        UserDocument savedUser = userRepository.save(user);
        RefreshToken refreshToken = createRefreshToken(savedUser,new Date(System.currentTimeMillis() + 50000));
        refreshTokenRepository.save(refreshToken);
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken());

        HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                AUTH_URL + "/refreshToken", entity, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getRefreshToken()).isNotNull();


    }

    @Test
    void generateTokenFromRefreshToken_fail_expiredToken() {
        try {
            UserDocument user = createUserDocument("user1@gmail.com", Role.SELLER);
            UserDocument savedUser = userRepository.save(user);
            RefreshToken refreshToken = createRefreshToken(savedUser,new Date(System.currentTimeMillis() - 50000));
            refreshTokenRepository.save(refreshToken);
            TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken());

            HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest);

            restTemplate.postForEntity(
                    AUTH_URL + "/refreshToken", entity, AuthResponse.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }  catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(e.getMessage()).contains(REFRESH_TOKEN_EXPIRED);
        }

    }

    @Test
    void generateTokenFromRefreshToken_fail_notFound() {
        try {

            String token = UtilClass.createRandomString();
            TokenRefreshRequest refreshRequest = new TokenRefreshRequest(token);

            HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest);

            restTemplate.postForEntity(
                    AUTH_URL + "/refreshToken", entity, AuthResponse.class);

            assertThat(1).isEqualTo(2); //will fail if exception is not thrown

        }  catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


    private RefreshToken createRefreshToken(UserDocument user, Date date) {
        return RefreshToken.builder()
                .token("123456789")
                .expirationDate(date)
                .user(user)
                .build();
    }
}