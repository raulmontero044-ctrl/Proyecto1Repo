package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;

public record PasswordEnClaro(String valor) {

    private static final int LONGITUD_MINIMA = 8;
    private static final int LONGITUD_MAXIMA = 72;

    public PasswordEnClaro {
        if (valor == null || !longitudValida(valor.codePointCount(0, valor.length()))) {
            throw new UsuarioInvalidoException(
                    "La contraseña debe tener entre " + LONGITUD_MINIMA + " y " + LONGITUD_MAXIMA + " caracteres");
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
