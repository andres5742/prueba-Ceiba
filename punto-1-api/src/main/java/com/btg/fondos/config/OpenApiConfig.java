package com.btg.fondos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "API Fondos BTG",
                        version = "1.0",
                        description = "Suscripciones, cancelaciones e historial. Mongo + JWT."))
public class OpenApiConfig {}
