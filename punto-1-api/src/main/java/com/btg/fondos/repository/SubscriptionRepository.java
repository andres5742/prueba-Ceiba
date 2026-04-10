package com.btg.fondos.repository;

import com.btg.fondos.domain.Subscription;
import com.btg.fondos.domain.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    Optional<Subscription> findBySubscriptionIdAndUserEmailAndStatus(
            String subscriptionId, String userEmail, SubscriptionStatus status);

    Optional<Subscription> findByUserEmailAndFundIdAndStatus(
            String userEmail, Integer fundId, SubscriptionStatus status);

    List<Subscription> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
