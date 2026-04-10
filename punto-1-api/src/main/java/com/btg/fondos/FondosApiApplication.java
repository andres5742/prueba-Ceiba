package com.btg.fondos;

import com.btg.fondos.config.AppProperties;
import com.btg.fondos.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class})
public class FondosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FondosApiApplication.class, args);
    }
}
