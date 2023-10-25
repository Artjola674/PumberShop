package com.ikubinfo.plumbershop.user.service.impl;

import com.ikubinfo.plumbershop.exception.TokenException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.ResetTokenRepository;
import com.ikubinfo.plumbershop.user.service.ResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.ikubinfo.plumbershop.common.constants.Constants.TOKEN;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.RESET_TOKEN;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.RESET_TOKEN_EXPIRED;

@Service
@RequiredArgsConstructor
public class ResetTokenServiceImpl implements ResetTokenService {

    private final ResetTokenRepository resetTokenRepository;


    @Value("${reset-token-expiration}")
    private long resetTokenExpirationTime;

    @Override
    public void createPasswordResetTokenForUser(UserDocument user, String token) {
        ResetTokenDocument resetTokenDocument = ResetTokenDocument
                .builder()
                .token(token)
                .user(user)
                .expirationDate(new Date(new Date().getTime() + resetTokenExpirationTime))
                .build();
        resetTokenRepository.save(resetTokenDocument);
    }

    @Override
    public ResetTokenDocument verifyToken(String token) {
        ResetTokenDocument resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(()-> new ResourceNotFoundException(RESET_TOKEN, TOKEN, token));

        checkIfTokenIsValid(resetToken);

        return resetToken;
    }

    private void checkIfTokenIsValid(ResetTokenDocument resetToken) {
        if (resetToken.getExpirationDate().toInstant().isBefore(new Date().toInstant())) {
            throw new TokenException(resetToken.getToken(),
                    RESET_TOKEN_EXPIRED);
        }
    }
}
