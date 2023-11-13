package com.ikubinfo.plumbershop.auth.repo;

import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canFindByToken() {
        RefreshToken refreshToken = createRefreshToken(new Date(),null);

        underTest.save(refreshToken);

        Optional<RefreshToken> result = underTest.findByToken(refreshToken.getToken());

        assertThat(result.get().getToken()).isEqualTo(refreshToken.getToken());
    }

    @Test
    void canNotFindByToken() {
        String token = "123456789";

        Optional<RefreshToken> result = underTest.findByToken(token);

        assertThat(result).isEmpty();
    }


    @Test
    void canFindByUserId() {
        UserDocument user = createUser();
        userRepository.save(user);

        RefreshToken refreshToken = createRefreshToken(new Date(),user);
        underTest.save(refreshToken);

        List<RefreshToken> result = underTest.findByUserId(user.getId());
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void canNotFindByUserId() {
        String id = "1";
        UserDocument user = createUser();
        userRepository.save(user);

        RefreshToken refreshToken = createRefreshToken(new Date(),user);
        underTest.save(refreshToken);

        List<RefreshToken> result = underTest.findByUserId(id);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void canFindByExpirationDateLessThanEqual() {
        Date date = new Date();
        RefreshToken refreshToken1 = createRefreshToken(new Date(date.getTime()+1000), null);
        RefreshToken refreshToken2 = createRefreshToken(date, null);
        RefreshToken refreshToken3 = createRefreshToken(new Date(date.getTime()-1000), null);
        List<RefreshToken> refreshTokenList = Arrays.asList(refreshToken1,refreshToken2,refreshToken3);
        underTest.saveAll(refreshTokenList);

        List<RefreshToken> result = underTest.findByExpirationDateLessThanEqual(date);

        assertThat(result.size()).isEqualTo(2);

        assertThat(result.get(0).getExpirationDate()).isNotEqualTo(new Date(date.getTime()+1000));
    }

    @Test
    void canNotFindByExpirationDateLessThanEqual() {
        Date date = new Date();
        RefreshToken refreshToken1 = createRefreshToken(new Date(date.getTime()+1000), null);
        RefreshToken refreshToken2 = createRefreshToken(date, null);
        RefreshToken refreshToken3 = createRefreshToken(new Date(date.getTime()-1000), null);
        List<RefreshToken> refreshTokenList = Arrays.asList(refreshToken1,refreshToken2,refreshToken3);
        underTest.saveAll(refreshTokenList);

        List<RefreshToken> result = underTest.findByExpirationDateLessThanEqual(new Date(date.getTime() - 2000));

        assertThat(result.isEmpty()).isTrue();

    }


    private RefreshToken createRefreshToken(Date date, UserDocument user) {
        return RefreshToken.builder()
                .token("123456789")
                .expirationDate(date)
                .user(user)
                .build();
    }

    private UserDocument createUser() {
        return UserDocument.builder()
                .firstName("Artjola")
                .lastName("Kotorri")
                .build();
    }

}