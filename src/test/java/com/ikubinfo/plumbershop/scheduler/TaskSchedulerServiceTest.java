package com.ikubinfo.plumbershop.scheduler;

import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.ResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class TaskSchedulerServiceTest {

    @Autowired
    private TaskSchedulerService underTest;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private ResetTokenRepository resetTokenRepository;

    @BeforeEach
    void setUp() {
        underTest = new TaskSchedulerServiceImpl(refreshTokenRepository, resetTokenRepository);
    }

    @Test
    void deleteExpiredTokens() {
        RefreshToken refreshToken = createRefreshToken();
        ResetTokenDocument resetToken = createResetToken();

        given(refreshTokenRepository
                .findByExpirationDateLessThanEqual(any())).willReturn(List.of(refreshToken));
        given(resetTokenRepository
                .findByExpirationDateLessThanEqual(any())).willReturn(List.of(resetToken));
        underTest.deleteExpiredTokens();

        verify(refreshTokenRepository).deleteAll(List.of(refreshToken));
        verify(resetTokenRepository).deleteAll(List.of(resetToken));

    }

    private RefreshToken createRefreshToken() {
        return RefreshToken.builder()
                .token("123456789")
                .expirationDate(new Date())
                .build();
    }

    private ResetTokenDocument createResetToken() {
        return ResetTokenDocument.builder()
                .token("123456789")
                .expirationDate(new Date())
                .build();
    }
}