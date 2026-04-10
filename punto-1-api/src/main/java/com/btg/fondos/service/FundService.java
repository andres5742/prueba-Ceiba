package com.btg.fondos.service;

import com.btg.fondos.dto.FundResponse;
import com.btg.fondos.repository.FundRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundService {

    private final FundRepository fundRepository;

    public List<FundResponse> listFunds() {
        return fundRepository.findAllByOrderByFundIdAsc().stream().map(FundResponse::from).toList();
    }
}
