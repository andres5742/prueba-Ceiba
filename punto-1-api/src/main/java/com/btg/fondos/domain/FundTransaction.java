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
@Document(collection = "transactions")
public class FundTransaction implements TransactionView {

    @Id
    private String transactionId;

    @Indexed
    private String userEmail;

    private TransactionType type;
    private Integer fundId;
    private String fundName;
    private long amountCop;
    private Instant createdAt;

    @Override
    public String id() {
        return transactionId;
    }

    @Override
    public Instant at() {
        return createdAt;
    }

    @Override
    public TransactionType type() {
        return type;
    }

    @Override
    public int fundId() {
        return fundId != null ? fundId : 0;
    }

    @Override
    public String fundName() {
        return fundName;
    }

    @Override
    public long amountCop() {
        return amountCop;
    }

    @Override
    public String userEmail() {
        return userEmail;
    }
}
