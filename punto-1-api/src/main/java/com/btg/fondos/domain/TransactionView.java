package com.btg.fondos.domain;

import java.time.Instant;

public interface TransactionView {

    String id();

    Instant at();

    TransactionType type();

    int fundId();

    String fundName();

    long amountCop();

    String userEmail();
}
