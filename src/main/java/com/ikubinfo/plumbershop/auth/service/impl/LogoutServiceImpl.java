package com.ikubinfo.plumbershop.auth.service.impl;

import com.ikubinfo.plumbershop.auth.repo.RefreshTokenRepository;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.security.JwtTokenProvider;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.ikubinfo.plumbershop.user.constants.UserConstants.USER;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.USERNAME;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String jwt = getTokenFromRequest(request);

        String email = jwtTokenProvider.getUsername(jwt);
        UserDocument user = getUser(email);
        refreshTokenRepository.findByUserId(user.getId())
                .forEach(refreshTokenRepository::delete);
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private UserDocument getUser(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER, USERNAME, email));
    }
}
