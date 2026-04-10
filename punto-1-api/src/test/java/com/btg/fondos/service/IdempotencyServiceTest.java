package com.btg.fondos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.btg.fondos.domain.IdempotencyRecord;
import com.btg.fondos.domain.SubscriptionStatus;
import com.btg.fondos.dto.SubscriptionResponse;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock private IdempotencyRecordRepository recordRepository;

    private IdempotencyService idempotencyService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        idempotencyService = new IdempotencyService(recordRepository, objectMapper);
    }

    @Test
    void noKeySkipsLookupAndSave() {
        assertThat(idempotencyService.cachedResponse("a@b.com", null, "subscribe:1", 1L)).isEmpty();
        assertThat(idempotencyService.cachedResponse("a@b.com", "   ", "subscribe:1", 1L)).isEmpty();

        idempotencyService.saveCachedResponse("a@b.com", null, "subscribe:1", 1L, exampleResponse());
        idempotencyService.saveCachedResponse("a@b.com", "", "subscribe:1", 1L, exampleResponse());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void secondCallReturnsSamePayload() throws Exception {
        String id = IdempotencyRecord.documentId("u@test.com", "key-1");
        SubscriptionResponse original = exampleResponse();
        String json = objectMapper.writeValueAsString(original);
        when(recordRepository.findById(id))
                .thenReturn(
                        Optional.of(
                                IdempotencyRecord.builder()
                                        .id(id)
                                        .operation("subscribe:1")
                                        .version(1L)
                                        .responseJson(json)
                                        .build()));

        Optional<SubscriptionResponse> out =
                idempotencyService.cachedResponse("u@test.com", "key-1", "subscribe:1", 1L);
        assertThat(out).isPresent();
        assertThat(out.get().subscriptionId()).isEqualTo(original.subscriptionId());
    }

    @Test
    void nullStoredVersionTreatedAsOne() throws Exception {
        String id = IdempotencyRecord.documentId("u@test.com", "key-legacy");
        SubscriptionResponse original = exampleResponse();
        String json = objectMapper.writeValueAsString(original);
        when(recordRepository.findById(id))
                .thenReturn(
                        Optional.of(
                                IdempotencyRecord.builder()
                                        .id(id)
                                        .operation("subscribe:1")
                                        .version(null)
                                        .responseJson(json)
                                        .build()));

        Optional<SubscriptionResponse> out =
                idempotencyService.cachedResponse("u@test.com", "key-legacy", "subscribe:1", 1L);
        assertThat(out).isPresent();
    }

    @Test
    void sameKeyDifferentOperationConflict() {
        String id = IdempotencyRecord.documentId("u@test.com", "key-1");
        when(recordRepository.findById(id))
                .thenReturn(
                        Optional.of(
                                IdempotencyRecord.builder()
                                        .id(id)
                                        .operation("subscribe:1")
                                        .version(1L)
                                        .responseJson("{}")
                                        .build()));

        assertThatThrownBy(
                        () ->
                                idempotencyService.cachedResponse(
                                        "u@test.com", "key-1", "subscribe:2", 1L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void versionMismatchThrows() {
        String id = IdempotencyRecord.documentId("u@test.com", "key-v");
        when(recordRepository.findById(id))
                .thenReturn(
                        Optional.of(
                                IdempotencyRecord.builder()
                                        .id(id)
                                        .operation("subscribe:1")
                                        .version(2L)
                                        .responseJson("{\"x\":1}")
                                        .build()));

        assertThatThrownBy(
                        () ->
                                idempotencyService.cachedResponse(
                                        "u@test.com", "key-v", "subscribe:1", 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Idempotency-Version no coincide");
    }

    @Test
    void requestVersionHigherThanStoredConflict() {
        String id = IdempotencyRecord.documentId("u@test.com", "key-v2");
        when(recordRepository.findById(id))
                .thenReturn(
                        Optional.of(
                                IdempotencyRecord.builder()
                                        .id(id)
                                        .operation("subscribe:1")
                                        .version(1L)
                                        .responseJson("{}")
                                        .build()));

        assertThatThrownBy(
                        () ->
                                idempotencyService.cachedResponse(
                                        "u@test.com", "key-v2", "subscribe:1", 3L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void savePersistsOperationVersionAndJson() throws Exception {
        idempotencyService.saveCachedResponse("u@test.com", "k1", "subscribe:3", 2L, exampleResponse());

        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(recordRepository).save(captor.capture());
        assertThat(captor.getValue().getOperation()).isEqualTo("subscribe:3");
        assertThat(captor.getValue().getVersion()).isEqualTo(2L);
        assertThat(captor.getValue().getResponseJson()).contains("sub-id");
    }

    @Test
    void versionFromHeadersDefaults() {
        assertThat(IdempotencyService.versionFromHeaders("key", null)).isEqualTo(1L);
        assertThat(IdempotencyService.versionFromHeaders("key", "")).isEqualTo(1L);
        assertThat(IdempotencyService.versionFromHeaders("key", "  ")).isEqualTo(1L);
    }

    @Test
    void versionFromHeadersParsesInteger() {
        assertThat(IdempotencyService.versionFromHeaders("k", "5")).isEqualTo(5L);
    }

    @Test
    void versionFromHeadersInvalid() {
        assertThatThrownBy(() -> IdempotencyService.versionFromHeaders("k", "x"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> IdempotencyService.versionFromHeaders("k", "0"))
                .isInstanceOf(ApiException.class);
    }

    private static SubscriptionResponse exampleResponse() {
        return new SubscriptionResponse(
                "sub-id", 1, "FONDO", 1000L, SubscriptionStatus.active, Instant.parse("2026-01-01T12:00:00Z"));
    }
}
