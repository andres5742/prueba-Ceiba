package com.btg.fondos.service;

import com.btg.fondos.domain.NotificationPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void notifySubscription(
            NotificationPreference channel,
            String email,
            String phonePlain,
            String fundName) {
        log.info(
                "Notificación de suscripción | fondo={} | cliente={} | canal={} | teléfono={}",
                fundName,
                email,
                channel,
                phonePlain != null ? phonePlain : "(no indicado)");
    }
}
