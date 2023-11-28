package com.ikubinfo.plumbershop.user.repo;

import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
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
class ResetTokenRepositoryTest {

    @Autowired
    private ResetTokenRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void canFindByToken() {
        ResetTokenDocument resetTokenDocument = createResetToken(new Date());
        underTest.save(resetTokenDocument);
        Optional<ResetTokenDocument> result = underTest.findByToken(resetTokenDocument.getToken());
        assertThat(result.get().getToken()).isEqualTo(resetTokenDocument.getToken());
    }

    @Test
    void canNotFindByToken() {
        String token = "123456789";
        Optional<ResetTokenDocument> result = underTest.findByToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    void canFindByExpirationDateLessThanEqual() {
        Date date = new Date();
        ResetTokenDocument resetToken1 = createResetToken(new Date(date.getTime()+1000));
        ResetTokenDocument resetToken2 = createResetToken(date);
        ResetTokenDocument resetToken3 = createResetToken(new Date(date.getTime()-1000));
        List<ResetTokenDocument> resetTokenList = Arrays.asList(resetToken1,resetToken2,resetToken3);
        underTest.saveAll(resetTokenList);

        List<ResetTokenDocument> result = underTest.findByExpirationDateLessThanEqual(date);

        assertThat(result.size()).isEqualTo(2);

        assertThat(result.get(0).getExpirationDate()).isNotEqualTo(new Date(date.getTime()+1000));


    }

    @Test
    void canNotFindByExpirationDateLessThanEqual() {
        Date date = new Date();
        ResetTokenDocument resetToken1 = createResetToken(new Date(date.getTime()+1000));
        ResetTokenDocument resetToken2 = createResetToken(date);
        ResetTokenDocument resetToken3 = createResetToken(new Date(date.getTime()-1000));
        List<ResetTokenDocument> resetTokenList = Arrays.asList(resetToken1,resetToken2,resetToken3);
        underTest.saveAll(resetTokenList);

        List<ResetTokenDocument> result = underTest.findByExpirationDateLessThanEqual(new Date(date.getTime() - 2000));

        assertThat(result.isEmpty()).isTrue();

    }

    private ResetTokenDocument createResetToken(Date date) {
        return ResetTokenDocument.builder()
                .token("123456789")
                .expirationDate(date)
                .build();
    }
}