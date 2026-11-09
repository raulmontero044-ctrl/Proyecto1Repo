package com.facturacion.auth.internal.domain.service;

import com.facturacion.auth.internal.domain.model.Rol;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class CalculadoraRolEfectivo {

    private CalculadoraRolEfectivo() {
    }

    public static Optional<Rol> calcular(boolean administrador, List<Rol> rolesEnEmpresas) {
        if (administrador) {
            return Optional.of(Rol.ADMINISTRADOR);
        }
        return rolesEnEmpresas.stream().min(Comparator.naturalOrder());
    }
}
