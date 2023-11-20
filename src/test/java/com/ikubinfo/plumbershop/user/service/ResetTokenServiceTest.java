package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.TokenException;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.ResetTokenRepository;
import com.ikubinfo.plumbershop.user.service.impl.ResetTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.PASSWORD_NOT_MATCH;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.RESET_TOKEN_EXPIRED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class ResetTokenServiceTest {

    @Autowired
    private ResetTokenService underTest;

    @Mock
    private ResetTokenRepository resetTokenRepository;

    @BeforeEach
    void setUp() {
        underTest = new ResetTokenServiceImpl(resetTokenRepository);
    }

    @Test
    void createPasswordResetTokenForUser() {
        UserDocument user = createUserDocument();

        underTest.createPasswordResetTokenForUser(user, "token");

        verify(resetTokenRepository).save(any(ResetTokenDocument.class));

    }

    @Test
    void verifyToken_success() {
        ResetTokenDocument resetTokenDocument = createResetToken(new Date(System.currentTimeMillis() + 5000));

        given(resetTokenRepository.findByToken(resetTokenDocument.getToken()))
                .willReturn(Optional.of(resetTokenDocument));

        ResetTokenDocument result = underTest.verifyToken(resetTokenDocument.getToken());

        assertThat(result.getToken()).isEqualTo(resetTokenDocument.getToken());
    }

    @Test
    void verifyToken_throwException_tokenExpired() {
        ResetTokenDocument resetTokenDocument = createResetToken(new Date());

        given(resetTokenRepository.findByToken(resetTokenDocument.getToken()))
                .willReturn(Optional.of(resetTokenDocument));

        assertThatThrownBy(() -> underTest.verifyToken(resetTokenDocument.getToken()))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("Failed for [%s]: %s",resetTokenDocument.getToken(),
                        RESET_TOKEN_EXPIRED);
    }

    private ResetTokenDocument createResetToken(Date date) {
        return ResetTokenDocument.builder()
                .token("123456789")
                .expirationDate(date)
                .build();
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
}