package com.ikubinfo.plumbershop.auth.service.impl;

import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.auth.service.AuthService;
import com.ikubinfo.plumbershop.exception.TokenException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.security.JwtTokenProvider;
import com.ikubinfo.plumbershop.user.model.UserDocument;

import com.ikubinfo.plumbershop.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import static com.ikubinfo.plumbershop.auth.constants.Constants.*;
import static com.ikubinfo.plumbershop.common.constants.Constants.TOKEN;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    @Value("${app-jwt-refresh-token-expiration}")
    private long jwtRefreshTokenExpirationDate;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication.getName());

        UserDocument user = userService.getUserByEmail(request.getEmail());
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse generateTokenFromRefreshToken(String refreshToken) {
        RefreshToken refreshTokenDoc = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new  ResourceNotFoundException(REFRESH_TOKEN,TOKEN,refreshToken));

        verifyExpiration(refreshTokenDoc);
        UserDocument user = refreshTokenDoc.getUser();
        String token = jwtTokenProvider.generateToken(user.getEmail());

            return AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .build();

    }

    public void verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpirationDate().toInstant().isBefore(new Date().toInstant())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException(refreshToken.getToken(),
                    REFRESH_TOKEN_EXPIRED);
        }
    }

    private String generateRefreshToken(UserDocument user){
        RefreshToken refreshToken = RefreshToken.builder()
                .expirationDate(new Date(new Date().getTime() + jwtRefreshTokenExpirationDate))
                .user(user)
                .token(UUID.randomUUID().toString())
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }


}
