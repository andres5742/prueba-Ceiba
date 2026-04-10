package com.btg.fondos.dto;

import com.btg.fondos.domain.TransactionType;
import com.btg.fondos.domain.TransactionView;
import java.time.Instant;

public record TransactionResponse(
        String transactionId,
        TransactionType type,
        int fundId,
        String fundName,
        long amountCop,
        Instant createdAt) {

    public static TransactionResponse from(TransactionView tx) {
        return new TransactionResponse(
                tx.id(),
                tx.type(),
                tx.fundId(),
                tx.fundName(),
                tx.amountCop(),
                tx.at());
    }
}
