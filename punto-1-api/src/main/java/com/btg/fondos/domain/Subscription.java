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
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String subscriptionId;

    @Indexed
    private String userEmail;

    private Integer fundId;
    private String fundName;
    private long lockedAmountCop;
    private SubscriptionStatus status;
    private Instant createdAt;
}
