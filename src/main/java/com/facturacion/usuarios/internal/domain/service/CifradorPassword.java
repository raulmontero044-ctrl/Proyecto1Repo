package com.facturacion.usuarios.internal.domain.service;

import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;

public interface CifradorPassword {

    String cifrar(PasswordEnClaro password);

    boolean coincide(String passwordEnClaro, String passwordHash);
}
