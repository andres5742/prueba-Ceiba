package com.btg.fondos.web;

import com.btg.fondos.dto.TransactionResponse;
import com.btg.fondos.security.UserPrincipal;
import com.btg.fondos.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public List<TransactionResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return subscriptionService.listTransactions(principal.getEmail()).stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
