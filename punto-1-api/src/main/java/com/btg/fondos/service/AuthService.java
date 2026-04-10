package com.btg.fondos.service;

import com.btg.fondos.domain.User;
import com.btg.fondos.dto.LoginRequest;
import com.btg.fondos.dto.TokenResponse;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.UserRepository;
import com.btg.fondos.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        User user =
                userRepository
                        .findByEmail(request.email().trim().toLowerCase())
                        .orElseThrow(() -> ApiException.unauthorized("Credenciales incorrectas"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciales incorrectas");
        }
        String token = jwtService.createAccessToken(user.getEmail(), user.getRole());
        return new TokenResponse(token, "bearer");
    }
}
