package com.ikubinfo.plumbershop.auth.controller;

import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.auth.dto.TokenRefreshRequest;
import com.ikubinfo.plumbershop.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.generateTokenFromRefreshToken(request.getRefreshToken()));
    }

}
