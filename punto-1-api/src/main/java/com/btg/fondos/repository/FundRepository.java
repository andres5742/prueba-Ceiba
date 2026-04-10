package com.btg.fondos.repository;

import com.btg.fondos.domain.Fund;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FundRepository extends MongoRepository<Fund, Integer> {

    List<Fund> findAllByOrderByFundIdAsc();
}
