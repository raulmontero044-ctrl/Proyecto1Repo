package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class PurgarRefreshTokens {

    private final RefreshTokenRepository repository;
    private final Clock reloj;

    PurgarRefreshTokens(RefreshTokenRepository repository, Clock reloj) {
        this.repository = repository;
        this.reloj = reloj;
    }

    @Transactional
    public void ejecutar() {
        repository.eliminarRevocadosOCaducados(reloj.instant());
    }
}
