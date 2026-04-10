package com.btg.fondos.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String email;

    private String passwordHash;
    private String phone;
    private NotificationPreference notificationPreference;
    private UserRole role;
    private long balanceCop;
}
