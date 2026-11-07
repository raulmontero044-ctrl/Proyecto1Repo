package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;

import java.nio.charset.StandardCharsets;

public record PasswordEnClaro(String valor) {

    private static final int LONGITUD_MINIMA = 8;
    private static final int LONGITUD_MAXIMA = 72;
    private static final int BYTES_MAXIMOS = 72;

    public PasswordEnClaro {
        if (valor == null || !longitudValida(valor.codePointCount(0, valor.length()))) {
            throw new UsuarioInvalidoException(
                    "La contraseña debe tener entre " + LONGITUD_MINIMA + " y " + LONGITUD_MAXIMA + " caracteres");
        }
        if (valor.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            throw new UsuarioInvalidoException(
                    "La contraseña no puede superar " + BYTES_MAXIMOS + " bytes en UTF-8");
        }
    }

    @Override
    public String toString() {
        return "PasswordEnClaro[protegida]";
    }

    private static boolean longitudValida(int longitud) {
        return longitud >= LONGITUD_MINIMA && longitud <= LONGITUD_MAXIMA;
    }
}
