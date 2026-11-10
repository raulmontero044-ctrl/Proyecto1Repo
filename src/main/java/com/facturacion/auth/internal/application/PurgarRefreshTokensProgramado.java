package com.facturacion.auth.internal.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class PurgarRefreshTokensProgramado {

    private final PurgarRefreshTokens purgarRefreshTokens;

    PurgarRefreshTokensProgramado(PurgarRefreshTokens purgarRefreshTokens) {
        this.purgarRefreshTokens = purgarRefreshTokens;
    }

    @Scheduled(cron = "${facturacion.refresh-token.purga-cron:0 0 3 * * *}")
    void purgar() {
        purgarRefreshTokens.ejecutar();
    }
}
