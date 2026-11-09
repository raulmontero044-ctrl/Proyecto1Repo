package com.facturacion.auth.internal.domain.model;

import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public record RefreshToken(Long id, Long usuarioId, String hash, Instant expiraEn, boolean revocado) {

    public static final Duration DURACION = Duration.ofDays(10);

    private static final Pattern HASH_SHA256 = Pattern.compile("[0-9a-f]{64}");

    public RefreshToken {
        if (usuarioId == null) {
            throw new RefreshTokenInvalidoException("El usuario del refresh token es obligatorio");
        }
        if (hash == null || !HASH_SHA256.matcher(hash).matches()) {
            throw new RefreshTokenInvalidoException("El hash del refresh token debe ser un SHA-256 hexadecimal");
        }
        if (expiraEn == null) {
            throw new RefreshTokenInvalidoException("La caducidad del refresh token es obligatoria");
        }
    }

    public static RefreshToken emitir(Long usuarioId, String hash, Instant ahora) {
        if (ahora == null) {
            throw new RefreshTokenInvalidoException("El instante de emisión del refresh token es obligatorio");
        }
        return new RefreshToken(null, usuarioId, hash, ahora.plus(DURACION), false);
    }

    public boolean vigenteEn(Instant instante) {
        return !revocado && instante.isBefore(expiraEn);
    }

    public RefreshToken revocar() {
        return new RefreshToken(id, usuarioId, hash, expiraEn, true);
    }
}
