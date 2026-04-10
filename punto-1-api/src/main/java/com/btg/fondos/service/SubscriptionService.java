package com.btg.fondos.service;

import com.btg.fondos.domain.Fund;
import com.btg.fondos.domain.FundTransaction;
import com.btg.fondos.domain.Subscription;
import com.btg.fondos.domain.SubscriptionStatus;
import com.btg.fondos.domain.TransactionIdGenerator;
import com.btg.fondos.domain.TransactionType;
import com.btg.fondos.domain.User;
import com.btg.fondos.exception.ApiException;
import com.btg.fondos.repository.FundRepository;
import com.btg.fondos.repository.FundTransactionRepository;
import com.btg.fondos.repository.SubscriptionRepository;
import com.btg.fondos.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final FundRepository fundRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FundTransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final FieldEncryptionService fieldEncryptionService;
    private final TransactionIdGenerator transactionIdGenerator;

    public Subscription subscribe(String userEmail, int fundId) {
        Fund fund =
                fundRepository
                        .findById(fundId)
                        .orElseThrow(() -> ApiException.notFound("Fondo"));
        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(() -> ApiException.notFound("Usuario"));

        if (subscriptionRepository
                .findByUserEmailAndFundIdAndStatus(userEmail, fundId, SubscriptionStatus.active)
                .isPresent()) {
            throw ApiException.conflict("Ya tiene una suscripción activa a este fondo");
        }

        long minAmount = fund.getMinAmountCop();
        if (user.getBalanceCop() < minAmount) {
            throw ApiException.insufficientBalance(fund.getName());
        }

        String subscriptionId = transactionIdGenerator.newId();
        Instant now = Instant.now();

        user.setBalanceCop(user.getBalanceCop() - minAmount);
        userRepository.save(user);

        Subscription subscription =
                Subscription.builder()
                        .subscriptionId(subscriptionId)
                        .userEmail(userEmail)
                        .fundId(fundId)
                        .fundName(fund.getName())
                        .lockedAmountCop(minAmount)
                        .status(SubscriptionStatus.active)
                        .createdAt(now)
                        .build();
        subscriptionRepository.save(subscription);

        recordTransaction(
                userEmail, TransactionType.subscribe, fundId, fund.getName(), minAmount, now);

        String phonePlain =
                user.getPhone() != null
                        ? fieldEncryptionService.decryptIfNeeded(user.getPhone())
                        : null;
        notificationService.notifySubscription(
                user.getNotificationPreference(), userEmail, phonePlain, fund.getName());

        return subscription;
    }

    public Subscription cancel(String userEmail, String subscriptionId) {
        Subscription subscription =
                subscriptionRepository
                        .findBySubscriptionIdAndUserEmailAndStatus(
                                subscriptionId, userEmail, SubscriptionStatus.active)
                        .orElseThrow(() -> ApiException.notFound("Suscripción"));

        long lockedAmount = subscription.getLockedAmountCop();
        Instant now = Instant.now();

        subscription.setStatus(SubscriptionStatus.cancelled);
        subscriptionRepository.save(subscription);

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        user.setBalanceCop(user.getBalanceCop() + lockedAmount);
        userRepository.save(user);

        recordTransaction(
                userEmail,
                TransactionType.cancel,
                subscription.getFundId(),
                subscription.getFundName(),
                lockedAmount,
                now);

        return subscription;
    }

    private void recordTransaction(
            String userEmail,
            TransactionType type,
            int fundId,
            String fundName,
            long amountCop,
            Instant createdAt) {
        String txId = transactionIdGenerator.newId();
        FundTransaction tx =
                FundTransaction.builder()
                        .transactionId(txId)
                        .userEmail(userEmail)
                        .type(type)
                        .fundId(fundId)
                        .fundName(fundName)
                        .amountCop(amountCop)
                        .createdAt(createdAt)
                        .build();
        transactionRepository.save(tx);
    }

    public List<Subscription> listSubscriptions(String userEmail) {
        return subscriptionRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<FundTransaction> listTransactions(String userEmail) {
        return transactionRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }
}
