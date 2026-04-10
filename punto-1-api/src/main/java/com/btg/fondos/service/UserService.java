package com.btg.fondos.service;

import com.btg.fondos.config.AppProperties;
import com.btg.fondos.domain.NotificationPreference;
import com.btg.fondos.domain.User;
import com.btg.fondos.domain.UserRole;
import com.btg.fondos.dto.RegisterRequest;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final FieldEncryptionService fieldEncryptionService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("El correo ya está registrado");
        }
        if (request.notificationPreference() == NotificationPreference.sms
                && (request.phone() == null || request.phone().isBlank())) {
            throw ApiException.conflict("Debe indicar teléfono para notificaciones por SMS");
        }
        String phoneStored =
                request.phone() != null && !request.phone().isBlank()
                        ? fieldEncryptionService.encryptIfNeeded(request.phone().trim())
                        : null;
        User user =
                User.builder()
                        .email(request.email().trim().toLowerCase())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .phone(phoneStored)
                        .notificationPreference(request.notificationPreference())
                        .role(UserRole.client)
                        .balanceCop(appProperties.getInitialBalanceCop())
                        .build();
        userRepository.save(user);
    }

    public User requireByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Usuario"));
    }
}
