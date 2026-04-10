package com.btg.fondos.dto;

import com.btg.fondos.domain.Fund;

public record FundResponse(int fundId, String name, long minAmountCop, String category) {

    public static FundResponse from(Fund fund) {
        return new FundResponse(
                fund.getFundId(), fund.getName(), fund.getMinAmountCop(), fund.getCategory());
    }
}
