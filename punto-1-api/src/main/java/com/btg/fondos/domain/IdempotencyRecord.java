package com.btg.fondos.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "idempotency_keys")
public class IdempotencyRecord {

    @Id private String id;

    @Indexed
    private String userEmail;

    private String idempotencyKey;
    private String operation;
    private Long version;

    private String responseJson;
    private Instant createdAt;

    public static String documentId(String email, String idempotencyKey) {
        return email.trim().toLowerCase() + "|" + idempotencyKey.trim();
    }
}
