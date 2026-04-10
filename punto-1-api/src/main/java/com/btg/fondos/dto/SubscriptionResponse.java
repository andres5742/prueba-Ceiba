package com.btg.fondos.dto;

import com.btg.fondos.domain.Subscription;
import com.btg.fondos.domain.SubscriptionStatus;
import java.time.Instant;

public record SubscriptionResponse(
        String subscriptionId,
        int fundId,
        String fundName,
        long lockedAmountCop,
        SubscriptionStatus status,
        Instant createdAt) {

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getFundId(),
                subscription.getFundName(),
                subscription.getLockedAmountCop(),
                subscription.getStatus(),
                subscription.getCreatedAt());
    }
}
