package com.ikubinfo.plumbershop.scheduler;

import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.repo.ResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerServiceImpl implements TaskSchedulerService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetTokenRepository resetTokenRepository;

    @Override
    @Scheduled(cron = "0 0 23 * * ?")
    public void deleteExpiredTokens() {
        log.debug("Deleting expired tokens");
        List<RefreshToken> refreshTokens = refreshTokenRepository
                .findByExpirationDateLessThanEqual(new Date());

        List<ResetTokenDocument> resetTokenDocuments = resetTokenRepository
                .findByExpirationDateLessThanEqual(new Date());
        refreshTokenRepository.deleteAll(refreshTokens);
        resetTokenRepository.deleteAll(resetTokenDocuments);
        log.debug("Token is deleted successfully");

    }
}
