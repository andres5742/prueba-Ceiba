package com.btg.fondos.service;

import com.btg.fondos.domain.IdempotencyRecord;
import com.btg.fondos.dto.SubscriptionResponse;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final int MAX_KEY_LENGTH = 256;

    private final IdempotencyRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    public static long versionFromHeaders(String idempotencyKey, String versionHeader) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return 1L;
        }
        if (!StringUtils.hasText(versionHeader)) {
            return 1L;
        }
        String t = versionHeader.trim();
        try {
            long v = Long.parseLong(t);
            if (v < 1L) {
                throw ApiException.badRequest("Idempotency-Version debe ser un entero >= 1");
            }
            return v;
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(
                    "Idempotency-Version debe ser un número entero (ej. 1, 2, 3)");
        }
    }

    static long storedVersion(IdempotencyRecord record) {
        Long v = record.getVersion();
        return v == null ? 1L : v;
    }

    public Optional<SubscriptionResponse> cachedResponse(
            String email, String idempotencyKey, String operation, long requestVersion) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return Optional.empty();
        }
        validateKeyLength(idempotencyKey);
        String id = IdempotencyRecord.documentId(email, idempotencyKey);
        Optional<IdempotencyRecord> existing = recordRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        IdempotencyRecord record = existing.get();
        if (!operation.equals(record.getOperation())) {
            throw ApiException.conflict(
                    "Idempotency-Key ya utilizada con otra operación; use una clave nueva.");
        }
        long stored = storedVersion(record);
        if (requestVersion != stored) {
            throw ApiException.conflict(
                    "Idempotency-Version no coincide con la petición ya procesada (almacenada: "
                            + stored
                            + ", recibida: "
                            + requestVersion
                            + "). Repita con la misma versión para obtener la respuesta en caché, "
                            + "o use una Idempotency-Key nueva. No se ejecutará de nuevo para evitar duplicados.");
        }
        try {
            return Optional.of(
                    objectMapper.readValue(record.getResponseJson(), SubscriptionResponse.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Respuesta idempotente corrupta", e);
        }
    }

    public void saveCachedResponse(
            String email,
            String idempotencyKey,
            String operation,
            long requestVersion,
            SubscriptionResponse response) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return;
        }
        validateKeyLength(idempotencyKey);
        String id = IdempotencyRecord.documentId(email, idempotencyKey);
        try {
            String json = objectMapper.writeValueAsString(response);
            recordRepository.save(
                    IdempotencyRecord.builder()
                            .id(id)
                            .userEmail(email.trim().toLowerCase())
                            .idempotencyKey(idempotencyKey.trim())
                            .operation(operation)
                            .version(requestVersion)
                            .responseJson(json)
                            .createdAt(Instant.now())
                            .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la respuesta idempotente", e);
        }
    }

    private static void validateKeyLength(String idempotencyKey) {
        if (idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw ApiException.badRequest(
                    "La cabecera Idempotency Key no puede superar " + MAX_KEY_LENGTH + " caracteres");
        }
    }
}
