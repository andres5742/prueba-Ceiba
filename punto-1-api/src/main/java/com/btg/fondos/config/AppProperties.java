package com.btg.fondos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private long initialBalanceCop = 500_000L;

    private String fieldEncryptionKey = "";
}
