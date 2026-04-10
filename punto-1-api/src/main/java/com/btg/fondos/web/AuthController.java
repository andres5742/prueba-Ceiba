package com.btg.fondos.web;

import com.btg.fondos.domain.User;
import com.btg.fondos.dto.LoginRequest;
import com.btg.fondos.dto.MessageResponse;
import com.btg.fondos.dto.RegisterRequest;
import com.btg.fondos.dto.TokenResponse;
import com.btg.fondos.dto.UserMeResponse;
import com.btg.fondos.security.UserPrincipal;
import com.btg.fondos.service.AuthService;
import com.btg.fondos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return new MessageResponse("Usuario registrado");
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserMeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.requireByEmail(principal.getEmail());
        return new UserMeResponse(
                user.getEmail(),
                user.getBalanceCop(),
                user.getNotificationPreference(),
                user.getRole());
    }
}
