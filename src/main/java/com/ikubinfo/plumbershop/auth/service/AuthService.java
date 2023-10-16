package com.ikubinfo.plumbershop.auth.service;

import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);

    AuthResponse generateTokenFromRefreshToken(String requestRefreshToken);

}
