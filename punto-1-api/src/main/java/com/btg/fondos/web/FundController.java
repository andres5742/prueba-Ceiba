package com.btg.fondos.web;

import com.btg.fondos.domain.Subscription;
import com.btg.fondos.dto.FundResponse;
import com.btg.fondos.dto.SubscriptionResponse;
import com.btg.fondos.security.UserPrincipal;
import com.btg.fondos.service.FundService;
import com.btg.fondos.service.IdempotencyService;
import com.btg.fondos.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;
    private final SubscriptionService subscriptionService;
    private final IdempotencyService idempotencyService;

    @GetMapping
    public List<FundResponse> listFunds() {
        return fundService.listFunds();
    }

    @PostMapping("/{fundId}/subscribe")
    public SubscriptionResponse subscribe(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable int fundId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "Idempotency-Version", required = false) String idempotencyVersion) {

        String email = principal.getEmail();
        String operation = "subscribe:" + fundId;
        long version = IdempotencyService.versionFromHeaders(idempotencyKey, idempotencyVersion);

        return idempotencyService
                .cachedResponse(email, idempotencyKey, operation, version)
                .orElseGet(
                        () -> {
                            Subscription sub = subscriptionService.subscribe(email, fundId);
                            SubscriptionResponse body = SubscriptionResponse.from(sub);
                            idempotencyService.saveCachedResponse(
                                    email, idempotencyKey, operation, version, body);
                            return body;
                        });
    }
}
