package com.facturacion.auth.internal.domain.repository;

import com.facturacion.auth.internal.domain.model.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken guardar(RefreshToken refreshToken);

    Optional<RefreshToken> buscarPorHash(String hash);

    List<RefreshToken> buscarVigentesPorUsuario(Long usuarioId, Instant instante);

    void eliminarRevocadosOCaducados(Instant instante);
}
