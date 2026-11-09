package com.facturacion.auth.internal.domain.repository;

import com.facturacion.auth.internal.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken guardar(RefreshToken refreshToken);

    Optional<RefreshToken> buscarPorHash(String hash);
}
