package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;

class CifradorPasswordFalso implements CifradorPassword {

    static String hashDe(String password) {
        return "hash:" + password;
    }

    @Override
    public String cifrar(PasswordEnClaro password) {
        return hashDe(password.valor());
    }

    @Override
    public boolean coincide(String passwordEnClaro, String passwordHash) {
        return passwordEnClaro != null && hashDe(passwordEnClaro).equals(passwordHash);
    }
}
