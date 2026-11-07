package com.facturacion.usuarios.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AccesoEmpresaEmbeddable {

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    protected AccesoEmpresaEmbeddable() {
    }

    public AccesoEmpresaEmbeddable(Long empresaId, String rol) {
        this.empresaId = empresaId;
        this.rol = rol;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getRol() {
        return rol;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof AccesoEmpresaEmbeddable acceso)) {
            return false;
        }
        return Objects.equals(empresaId, acceso.empresaId) && Objects.equals(rol, acceso.rol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, rol);
    }
}
