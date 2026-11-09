package com.facturacion.auth.internal.infrastructure;

import com.facturacion.auth.internal.domain.service.GeneradorRefreshToken;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
class SecureRandomGeneradorRefreshToken implements GeneradorRefreshToken {

    static final int BYTES_ALEATORIOS = 32;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String generar() {
        byte[] bytes = new byte[BYTES_ALEATORIOS];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
