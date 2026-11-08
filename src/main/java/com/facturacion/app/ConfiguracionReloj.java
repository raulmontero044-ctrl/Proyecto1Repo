package com.facturacion.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ConfiguracionReloj {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
