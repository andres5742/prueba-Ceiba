package com.btg.fondos.web;

import com.btg.fondos.domain.Subscription;
import com.btg.fondos.dto.SubscriptionResponse;
import com.btg.fondos.security.UserPrincipal;
import com.btg.fondos.service.IdempotencyService;
import com.btg.fondos.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final IdempotencyService idempotencyService;

    @GetMapping
    public List<SubscriptionResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return subscriptionService.listSubscriptions(principal.getEmail()).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @DeleteMapping("/{subscriptionId}")
    public SubscriptionResponse cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String subscriptionId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "Idempotency-Version", required = false) String idempotencyVersion) {

        String email = principal.getEmail();
        String operation = "cancel:" + subscriptionId;
        long version = IdempotencyService.versionFromHeaders(idempotencyKey, idempotencyVersion);

        return idempotencyService
                .cachedResponse(email, idempotencyKey, operation, version)
                .orElseGet(
                        () -> {
                            Subscription sub = subscriptionService.cancel(email, subscriptionId);
                            SubscriptionResponse body = SubscriptionResponse.from(sub);
                            idempotencyService.saveCachedResponse(
                                    email, idempotencyKey, operation, version, body);
                            return body;
                        });
    }
}
