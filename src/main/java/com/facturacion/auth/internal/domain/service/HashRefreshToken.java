package com.facturacion.auth.internal.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashRefreshToken {

    private HashRefreshToken() {
    }

    public static String calcular(String valor) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("La plataforma Java no ofrece SHA-256", excepcion);
        }
    }
}
