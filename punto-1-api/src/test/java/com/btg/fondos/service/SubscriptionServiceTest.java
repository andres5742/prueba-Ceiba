package com.btg.fondos.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.btg.fondos.domain.Fund;
import com.btg.fondos.domain.NotificationPreference;
import com.btg.fondos.domain.SubscriptionStatus;
import com.btg.fondos.domain.TransactionIdGenerator;
import com.btg.fondos.domain.User;
import com.btg.fondos.domain.UserRole;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.FundRepository;
import com.btg.fondos.repository.FundTransactionRepository;
import com.btg.fondos.repository.SubscriptionRepository;
import com.btg.fondos.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private FundRepository fundRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private FundTransactionRepository transactionRepository;
    @Mock private NotificationService notificationService;
    @Mock private FieldEncryptionService fieldEncryptionService;
    @Mock private TransactionIdGenerator transactionIdGenerator;

    @InjectMocks private SubscriptionService subscriptionService;

    @Test
    void subscribeFailsWhenInsufficientBalance() {
        Fund fund =
                Fund.builder()
                        .fundId(4)
                        .name("FDO-ACCIONES")
                        .minAmountCop(250_000L)
                        .category("FIC")
                        .build();
        User user =
                User.builder()
                        .email("u@test.com")
                        .balanceCop(100_000L)
                        .notificationPreference(NotificationPreference.email)
                        .role(UserRole.client)
                        .build();

        when(fundRepository.findById(4)).thenReturn(Optional.of(fund));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserEmailAndFundIdAndStatus(
                        "u@test.com", 4, SubscriptionStatus.active))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.subscribe("u@test.com", 4))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No tiene saldo disponible para vincularse al fondo FDO-ACCIONES");

        verify(userRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(notificationService, never())
                .notifySubscription(any(), anyString(), anyString(), anyString());
    }
}
