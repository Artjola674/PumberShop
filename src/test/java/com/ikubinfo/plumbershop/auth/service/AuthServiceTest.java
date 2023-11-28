package com.ikubinfo.plumbershop.auth.service;

import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.auth.service.impl.AuthServiceImpl;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.TokenException;
import com.ikubinfo.plumbershop.security.JwtTokenProvider;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Optional;

import static com.ikubinfo.plumbershop.auth.constants.Constants.REFRESH_TOKEN_EXPIRED;
import static com.ikubinfo.plumbershop.common.constants.BadRequest.PASSWORD_NOT_MATCH;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService underTest;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        underTest = new AuthServiceImpl(authenticationManager,jwtTokenProvider,refreshTokenRepository,userService);
    }

    @Test
    void generateTokenFromRefreshToken_success() {
        UserDocument user = createUserDocument();
        RefreshToken refreshToken = createRefreshToken(user,new Date(System.currentTimeMillis()+5000));
        String accessToken = "token";

        given(refreshTokenRepository.findByToken(refreshToken.getToken()))
                .willReturn(Optional.of(refreshToken));
        given(jwtTokenProvider.generateToken(user.getEmail())).willReturn(accessToken);

        AuthResponse result = underTest.generateTokenFromRefreshToken(refreshToken.getToken());

        assertThat(result.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    void generateTokenFromRefreshToken_throwException_ExpiredToken() {

        UserDocument user = createUserDocument();
        RefreshToken refreshToken = createRefreshToken(user,new Date());
        String accessToken = "token";

        given(refreshTokenRepository.findByToken(refreshToken.getToken()))
                .willReturn(Optional.of(refreshToken));

        assertThatThrownBy(() ->underTest.generateTokenFromRefreshToken(refreshToken.getToken()))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Failed for [%s]: %s",refreshToken.getToken(),
                        REFRESH_TOKEN_EXPIRED);

        verify(refreshTokenRepository).delete(refreshToken);
        verify(jwtTokenProvider,never()).generateToken(user.getEmail());
    }

    private UserDocument createUserDocument() {
        UserDocument userDocument = new UserDocument();
        userDocument.setFirstName("Artjola1");
        userDocument.setLastName("Kotorri1");
        userDocument.setEmail("artjolakotorri1@gmail.com");
        userDocument.setRole(Role.ADMIN);
        userDocument.setPassword("1A@a2345678");
        return userDocument;
    }

    private RefreshToken createRefreshToken(UserDocument user, Date date) {
        return RefreshToken.builder()
                .token("123456789")
                .expirationDate(date)
                .user(user)
                .build();
    }
}