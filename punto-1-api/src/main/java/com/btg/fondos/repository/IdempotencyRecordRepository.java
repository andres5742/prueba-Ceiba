package com.btg.fondos.repository;

import com.btg.fondos.domain.IdempotencyRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdempotencyRecordRepository extends MongoRepository<IdempotencyRecord, String> {}
