package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class RefreshTokenRepositoryEnMemoria implements RefreshTokenRepository {

    private final Map<Long, RefreshToken> tokens = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public RefreshToken guardar(RefreshToken refreshToken) {
        Long id = refreshToken.id() != null ? refreshToken.id() : siguienteId++;
        RefreshToken guardado = new RefreshToken(
                id, refreshToken.usuarioId(), refreshToken.hash(), refreshToken.expiraEn(), refreshToken.revocado());
        tokens.put(id, guardado);
        return guardado;
    }

    @Override
    public Optional<RefreshToken> buscarPorHash(String hash) {
        return tokens.values().stream().filter(token -> token.hash().equals(hash)).findFirst();
    }

    @Override
    public List<RefreshToken> buscarVigentesPorUsuario(Long usuarioId, Instant instante) {
        return tokens.values().stream()
                .filter(token -> token.usuarioId().equals(usuarioId) && token.vigenteEn(instante))
                .toList();
    }

    @Override
    public void eliminarRevocadosOCaducados(Instant instante) {
        tokens.values().removeIf(token -> !token.vigenteEn(instante));
    }

    List<RefreshToken> todos() {
        return new ArrayList<>(tokens.values());
    }
}
