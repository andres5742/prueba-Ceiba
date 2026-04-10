package com.btg.fondos.dto;

import com.btg.fondos.domain.NotificationPreference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        String phone,
        @NotNull NotificationPreference notificationPreference) {}
