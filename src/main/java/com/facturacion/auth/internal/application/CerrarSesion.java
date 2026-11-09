package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import com.facturacion.auth.internal.domain.service.HashRefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CerrarSesion {

    private final RefreshTokenRepository repository;

    CerrarSesion(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        repository.buscarPorHash(HashRefreshToken.calcular(refreshToken))
                .filter(token -> !token.revocado())
                .map(RefreshToken::revocar)
                .ifPresent(repository::guardar);
    }
}
