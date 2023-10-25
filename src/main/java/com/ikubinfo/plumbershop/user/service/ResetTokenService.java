package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;

public interface ResetTokenService {
    void createPasswordResetTokenForUser(UserDocument user, String token);

    ResetTokenDocument verifyToken(String token);
}
