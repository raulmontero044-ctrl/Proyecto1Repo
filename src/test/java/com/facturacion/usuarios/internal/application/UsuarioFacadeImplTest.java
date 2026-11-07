package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioFacadeImplTest {

    private final UsuarioRepositoryEnMemoria repository = new UsuarioRepositoryEnMemoria();
    private final CifradorPasswordFalso cifrador = new CifradorPasswordFalso();
    private final UsuarioFacade facade = new UsuarioFacadeImpl(
            new BuscarUsuario(repository),
            new BuscarUsuarioPorEmail(repository),
            new VerificarCredencialesUsuario(repository, cifrador),
            new CambiarPasswordUsuario(repository, cifrador));

    private Long guardado(String email, boolean activo) {
        return repository.guardar(new Usuario(
                null, email, "Ana", CifradorPasswordFalso.hashDe("secreta123"), activo,
                List.of(new AccesoEmpresa(3L, Rol.ADMINISTRADOR)))).id();
    }

    @Test
    void buscarDevuelveElUsuarioConSusAccesos() {
        Long id = guardado("ana@empresa.es", false);

        assertThat(facade.buscar(id)).contains(new UsuarioDto(
                id, "ana@empresa.es", "Ana", false, List.of(new AccesoEmpresaDto(3L, "ADMINISTRADOR"))));
    }

    @Test
    void buscarUnUsuarioInexistenteDevuelveVacioSinLanzarExcepcion() {
        assertThat(facade.buscar(99L)).isEmpty();
    }

    @Test
    void buscarPorEmailNormalizaElEmail() {
        Long id = guardado("ana@empresa.es", true);

        assertThat(facade.buscarPorEmail("ANA@EMPRESA.ES")).map(UsuarioDto::id).contains(id);
        assertThat(facade.buscarPorEmail("nadie@empresa.es")).isEmpty();
    }

    @Test
    void verificarCredencialesSoloDevuelveUsuariosActivosConLaContrasenaCorrecta() {
        Long activo = guardado("ana@empresa.es", true);
        guardado("inactivo@empresa.es", false);

        assertThat(facade.verificarCredenciales("ana@empresa.es", "secreta123")).map(UsuarioDto::id).contains(activo);
        assertThat(facade.verificarCredenciales("ana@empresa.es", "incorrecta")).isEmpty();
        assertThat(facade.verificarCredenciales("inactivo@empresa.es", "secreta123")).isEmpty();
        assertThat(facade.verificarCredenciales("nadie@empresa.es", "secreta123")).isEmpty();
    }

    @Test
    void cambiarPasswordPermiteVerificarConLaNueva() {
        Long id = guardado("ana@empresa.es", true);

        facade.cambiarPassword(id, "nuevaClave1");

        assertThat(facade.verificarCredenciales("ana@empresa.es", "nuevaClave1")).isPresent();
        assertThat(facade.verificarCredenciales("ana@empresa.es", "secreta123")).isEmpty();
    }

    @Test
    void cambiarPasswordAplicaLasMismasReglasDeLongitud() {
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> facade.cambiarPassword(id, "corta"))
                .isInstanceOf(UsuarioInvalidoException.class);
        assertThatThrownBy(() -> facade.cambiarPassword(id, "a".repeat(73)))
                .isInstanceOf(UsuarioInvalidoException.class);
        assertThat(facade.verificarCredenciales("ana@empresa.es", "secreta123")).isPresent();
    }

    @Test
    void cambiarPasswordDeUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> facade.cambiarPassword(99L, "nuevaClave1"))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }
}
