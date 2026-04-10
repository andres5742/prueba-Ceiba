package com.btg.fondos.repository;

import com.btg.fondos.domain.FundTransaction;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FundTransactionRepository extends MongoRepository<FundTransaction, String> {

    List<FundTransaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
