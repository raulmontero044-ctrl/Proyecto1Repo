package com.facturacion.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "facturacion.jwt")
public record PropiedadesJwt(String secreto) {

    static final int BYTES_MINIMOS_SECRETO = 32;

    public PropiedadesJwt {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < BYTES_MINIMOS_SECRETO) {
            throw new IllegalStateException("La propiedad facturacion.jwt.secreto (variable JWT_SECRET) debe tener al menos "
                    + BYTES_MINIMOS_SECRETO + " bytes para firmar con HS256");
        }
    }

    @Override
    public String toString() {
        return "PropiedadesJwt[secreto=protegido]";
    }
}
