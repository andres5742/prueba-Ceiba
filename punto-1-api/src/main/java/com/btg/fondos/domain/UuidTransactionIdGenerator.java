package com.btg.fondos.domain;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidTransactionIdGenerator implements TransactionIdGenerator {

    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }
}
