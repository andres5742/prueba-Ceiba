package com.btg.fondos.dto;

import com.btg.fondos.domain.NotificationPreference;
import com.btg.fondos.domain.UserRole;

public record UserMeResponse(
        String email,
        long balanceCop,
        NotificationPreference notificationPreference,
        UserRole role) {}
