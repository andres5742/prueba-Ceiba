package com.btg.fondos.bootstrap;

import com.btg.fondos.domain.Fund;
import com.btg.fondos.repository.FundRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundCatalogSeed implements ApplicationRunner {

    private final FundRepository fundRepository;

    private static final List<Fund> CATALOG =
            List.of(
                    Fund.builder()
                            .fundId(1)
                            .name("FPV_BTG_PACTUAL_RECAUDADORA")
                            .minAmountCop(75_000L)
                            .category("FPV")
                            .build(),
                    Fund.builder()
                            .fundId(2)
                            .name("FPV_BTG_PACTUAL_ECOPETROL")
                            .minAmountCop(125_000L)
                            .category("FPV")
                            .build(),
                    Fund.builder()
                            .fundId(3)
                            .name("DEUDAPRIVADA")
                            .minAmountCop(50_000L)
                            .category("FIC")
                            .build(),
                    Fund.builder()
                            .fundId(4)
                            .name("FDO-ACCIONES")
                            .minAmountCop(250_000L)
                            .category("FIC")
                            .build(),
                    Fund.builder()
                            .fundId(5)
                            .name("FPV_BTG_PACTUAL_DINAMICA")
                            .minAmountCop(100_000L)
                            .category("FPV")
                            .build());

    @Override
    public void run(ApplicationArguments args) {
        if (fundRepository.count() == 0) {
            fundRepository.saveAll(CATALOG);
        }
    }
}
