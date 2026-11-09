package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;

class CrearAdministradorInicialAlArrancarTest {

    private final UsuarioRepositoryEnMemoria repository = new UsuarioRepositoryEnMemoria();
    private final CrearAdministradorInicial crearAdministradorInicial =
            new CrearAdministradorInicial(repository, new CifradorPasswordFalso());

    private void arrancarCon(String email, String password) {
        new CrearAdministradorInicialAlArrancar(crearAdministradorInicial, email, password)
                .run(new DefaultApplicationArguments());
    }

    @Test
    void conLasVariablesDefinidasCreaElAdministradorConfigurado() {
        arrancarCon("admin@empresa.es", "secreta123");

        assertThat(repository.buscarPorEmail("admin@empresa.es")).hasValueSatisfying(usuario -> {
            assertThat(usuario.administrador()).isTrue();
            assertThat(usuario.activo()).isTrue();
            assertThat(usuario.passwordHash()).isEqualTo("hash:secreta123");
        });
    }

    @Test
    void conLasVariablesVaciasNoCreaNingunUsuario() {
        arrancarCon("", "");

        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void enArranquesPosterioresNoCreaOtroAdministrador() {
        arrancarCon("admin@empresa.es", "secreta123");
        arrancarCon("otro@empresa.es", "secreta123");

        assertThat(repository.listar()).extracting(Usuario::email).containsExactly("admin@empresa.es");
    }
}
