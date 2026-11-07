package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccesoEmpresaTest {

    @Test
    void creaUnAccesoConEmpresaYRol() {
        AccesoEmpresa acceso = new AccesoEmpresa(3L, Rol.ADMINISTRADOR);

        assertThat(acceso.empresaId()).isEqualTo(3L);
        assertThat(acceso.rol()).isEqualTo(Rol.ADMINISTRADOR);
    }

    @Test
    void rechazaEmpresaNula() {
        assertThatThrownBy(() -> new AccesoEmpresa(null, Rol.GESTOR))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La empresa del acceso es obligatoria");
    }

    @Test
    void rechazaRolNulo() {
        assertThatThrownBy(() -> new AccesoEmpresa(3L, null))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El rol del acceso es obligatorio");
    }

    @Test
    void losRolesSonAdministradorGestorYConsulta() {
        assertThat(Rol.values()).containsExactly(Rol.ADMINISTRADOR, Rol.GESTOR, Rol.CONSULTA);
    }
}
