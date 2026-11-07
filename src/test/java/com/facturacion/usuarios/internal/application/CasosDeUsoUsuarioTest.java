package com.facturacion.usuarios.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.DatosUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.NuevoUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.AccesoEmpresaNoEncontradoException;
import com.facturacion.usuarios.internal.domain.exception.EmailUsuarioDuplicadoException;
import com.facturacion.usuarios.internal.domain.exception.EmpresaInexistenteException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoUsuarioTest {

    private static final Set<Long> EMPRESAS_EXISTENTES = Set.of(1L, 2L);

    private final UsuarioRepositoryEnMemoria repository = new UsuarioRepositoryEnMemoria();
    private final CifradorPasswordFalso cifrador = new CifradorPasswordFalso();
    private final List<Long> empresasConsultadas = new ArrayList<>();
    private final EmpresaFacade empresaFacade = id -> {
        empresasConsultadas.add(id);
        return EMPRESAS_EXISTENTES.contains(id) ? Optional.of(empresa(id)) : Optional.empty();
    };
    private final ComprobadorEmpresa comprobadorEmpresa = new ComprobadorEmpresa(empresaFacade);

    private final CrearUsuario crearUsuario = new CrearUsuario(repository, cifrador, comprobadorEmpresa);
    private final ObtenerUsuario obtenerUsuario = new ObtenerUsuario(repository);
    private final ListarUsuarios listarUsuarios = new ListarUsuarios(repository);
    private final ModificarUsuario modificarUsuario = new ModificarUsuario(repository);
    private final DesactivarUsuario desactivarUsuario = new DesactivarUsuario(repository);
    private final AsignarEmpresaUsuario asignarEmpresaUsuario = new AsignarEmpresaUsuario(repository, comprobadorEmpresa);
    private final RetirarEmpresaUsuario retirarEmpresaUsuario = new RetirarEmpresaUsuario(repository);
    private final BuscarUsuario buscarUsuario = new BuscarUsuario(repository);
    private final BuscarUsuarioPorEmail buscarUsuarioPorEmail = new BuscarUsuarioPorEmail(repository);
    private final VerificarCredencialesUsuario verificarCredenciales = new VerificarCredencialesUsuario(repository, cifrador);
    private final CambiarPasswordUsuario cambiarPassword = new CambiarPasswordUsuario(repository, cifrador);

    private static EmpresaDto empresa(Long id) {
        return new EmpresaDto(
                id, "Empresa SL", "B1234567" + id, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                null, null, true);
    }

    private static NuevoUsuarioDto nuevo(String email, List<AccesoEmpresaDto> accesos) {
        return new NuevoUsuarioDto(email, "Ana", "secreta123", accesos);
    }

    private Long guardado(String email, boolean activo, AccesoEmpresa... accesos) {
        return repository.guardar(new Usuario(
                null, email, "Ana", CifradorPasswordFalso.hashDe("secreta123"), activo, List.of(accesos))).id();
    }

    @Test
    void crearDevuelveElUsuarioActivoConSusAccesosYGuardaElHash() {
        UsuarioDto creado = crearUsuario.ejecutar(new NuevoUsuarioDto(
                "Ana@Empresa.es", "Ana", "secreta123",
                List.of(new AccesoEmpresaDto(1L, "ADMINISTRADOR"), new AccesoEmpresaDto(2L, "CONSULTA"))));

        assertThat(creado).isEqualTo(new UsuarioDto(1L, "ana@empresa.es", "Ana", true, List.of(
                new AccesoEmpresaDto(1L, "ADMINISTRADOR"), new AccesoEmpresaDto(2L, "CONSULTA"))));
        assertThat(repository.buscarPorId(1L)).map(Usuario::passwordHash).contains("hash:secreta123");
        assertThat(empresasConsultadas).containsExactly(1L, 2L);
    }

    @Test
    void crearSinAccesosCreaUnUsuarioSinEmpresas() {
        UsuarioDto creado = crearUsuario.ejecutar(nuevo("ana@empresa.es", null));

        assertThat(creado.accesos()).isEmpty();
        assertThat(empresasConsultadas).isEmpty();
    }

    @Test
    void crearRechazaUnEmailDuplicadoSinDistinguirMayusculas() {
        guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ANA@empresa.es", List.of())))
                .isInstanceOf(EmailUsuarioDuplicadoException.class)
                .hasMessage("Ya existe otro usuario con ese email");
        assertThat(repository.listar()).hasSize(1);
    }

    @Test
    void crearRechazaUnaEmpresaInexistenteSinGuardar() {
        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ana@empresa.es", List.of(
                new AccesoEmpresaDto(1L, "GESTOR"), new AccesoEmpresaDto(9L, "GESTOR")))))
                .isInstanceOf(EmpresaInexistenteException.class)
                .hasMessage("No existe la empresa con id 9");
        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void crearRechazaUnaContrasenaFueraDeLongitud() {
        NuevoUsuarioDto corta = new NuevoUsuarioDto("ana@empresa.es", "Ana", "1234567", List.of());
        NuevoUsuarioDto larga = new NuevoUsuarioDto("ana@empresa.es", "Ana", "a".repeat(73), List.of());

        assertThatThrownBy(() -> crearUsuario.ejecutar(corta))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La contraseña debe tener entre 8 y 72 caracteres");
        assertThatThrownBy(() -> crearUsuario.ejecutar(larga)).isInstanceOf(UsuarioInvalidoException.class);
        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void crearAceptaContrasenasEnLosLimites() {
        crearUsuario.ejecutar(new NuevoUsuarioDto("a@empresa.es", "Ana", "12345678", List.of()));
        crearUsuario.ejecutar(new NuevoUsuarioDto("b@empresa.es", "Ana", "a".repeat(72), List.of()));

        assertThat(repository.listar()).hasSize(2);
    }

    @Test
    void crearRechazaUnRolInvalido() {
        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ana@empresa.es", List.of(new AccesoEmpresaDto(1L, "JEFE")))))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El rol del acceso no es válido");
        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ana@empresa.es", List.of(new AccesoEmpresaDto(1L, null)))))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El rol del acceso es obligatorio");
    }

    @Test
    void crearRechazaUnAccesoNulo() {
        List<AccesoEmpresaDto> accesos = Arrays.asList(new AccesoEmpresaDto(1L, "GESTOR"), null);

        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ana@empresa.es", accesos)))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("Los accesos del usuario no pueden ser nulos");
    }

    @Test
    void crearRechazaDosAccesosALaMismaEmpresa() {
        assertThatThrownBy(() -> crearUsuario.ejecutar(nuevo("ana@empresa.es", List.of(
                new AccesoEmpresaDto(1L, "GESTOR"), new AccesoEmpresaDto(1L, "CONSULTA")))))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El usuario solo puede tener un acceso por empresa");
        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void obtenerDevuelveElUsuarioSinHash() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));

        assertThat(obtenerUsuario.ejecutar(id)).isEqualTo(new UsuarioDto(
                id, "ana@empresa.es", "Ana", true, List.of(new AccesoEmpresaDto(1L, "GESTOR"))));
    }

    @Test
    void obtenerUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> obtenerUsuario.ejecutar(99L))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessage("No existe el usuario con id 99");
    }

    @Test
    void listarSinFiltroDevuelveTodos() {
        guardado("a@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));
        guardado("b@empresa.es", false);

        assertThat(listarUsuarios.ejecutar(null)).extracting(UsuarioDto::email)
                .containsExactly("a@empresa.es", "b@empresa.es");
    }

    @Test
    void listarPorEmpresaSoloDevuelveLosUsuariosConAccesoAElla() {
        guardado("a@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));
        guardado("b@empresa.es", true, new AccesoEmpresa(2L, Rol.GESTOR));
        guardado("c@empresa.es", true, new AccesoEmpresa(2L, Rol.CONSULTA), new AccesoEmpresa(1L, Rol.GESTOR));

        assertThat(listarUsuarios.ejecutar(1L)).extracting(UsuarioDto::email)
                .containsExactly("a@empresa.es", "c@empresa.es");
        assertThat(listarUsuarios.ejecutar(3L)).isEmpty();
    }

    @Test
    void modificarCambiaNombreYEmailNormalizado() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));

        UsuarioDto modificado = modificarUsuario.ejecutar(id, new DatosUsuarioDto("Ana María", "ANA.MARIA@empresa.es"));

        assertThat(modificado).isEqualTo(new UsuarioDto(
                id, "ana.maria@empresa.es", "Ana María", true, List.of(new AccesoEmpresaDto(1L, "GESTOR"))));
        assertThat(repository.buscarPorId(id)).map(Usuario::passwordHash).contains("hash:secreta123");
    }

    @Test
    void modificarPermiteConservarElPropioEmail() {
        Long id = guardado("ana@empresa.es", true);

        UsuarioDto modificado = modificarUsuario.ejecutar(id, new DatosUsuarioDto("Otra", "ANA@empresa.es"));

        assertThat(modificado.nombre()).isEqualTo("Otra");
    }

    @Test
    void modificarRechazaElEmailDeOtroUsuario() {
        guardado("otro@empresa.es", true);
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> modificarUsuario.ejecutar(id, new DatosUsuarioDto("Ana", "Otro@Empresa.es")))
                .isInstanceOf(EmailUsuarioDuplicadoException.class);
        assertThat(repository.buscarPorId(id)).map(Usuario::email).contains("ana@empresa.es");
    }

    @Test
    void modificarUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> modificarUsuario.ejecutar(99L, new DatosUsuarioDto("Ana", "ana@empresa.es")))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void modificarRechazaDatosInvalidos() {
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> modificarUsuario.ejecutar(id, new DatosUsuarioDto(" ", "ana@empresa.es")))
                .isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void desactivarMarcaSoloElUsuarioIndicadoComoInactivo() {
        Long otro = guardado("otro@empresa.es", true);
        Long id = guardado("ana@empresa.es", true);

        desactivarUsuario.ejecutar(id);

        assertThat(repository.buscarPorId(id)).map(Usuario::activo).contains(false);
        assertThat(repository.buscarPorId(otro)).map(Usuario::activo).contains(true);
    }

    @Test
    void desactivarUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> desactivarUsuario.ejecutar(99L)).isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void asignarAnadeUnAccesoNuevo() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));

        UsuarioDto usuario = asignarEmpresaUsuario.ejecutar(id, 2L, "CONSULTA");

        assertThat(usuario.accesos()).containsExactly(
                new AccesoEmpresaDto(1L, "GESTOR"), new AccesoEmpresaDto(2L, "CONSULTA"));
        assertThat(repository.buscarPorId(id).orElseThrow().accesos()).hasSize(2);
    }

    @Test
    void asignarCambiaElRolDeUnAccesoExistente() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));

        UsuarioDto usuario = asignarEmpresaUsuario.ejecutar(id, 1L, "ADMINISTRADOR");

        assertThat(usuario.accesos()).containsExactly(new AccesoEmpresaDto(1L, "ADMINISTRADOR"));
    }

    @Test
    void asignarUnaEmpresaInexistenteLanzaExcepcionSinModificar() {
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> asignarEmpresaUsuario.ejecutar(id, 9L, "GESTOR"))
                .isInstanceOf(EmpresaInexistenteException.class)
                .hasMessage("No existe la empresa con id 9");
        assertThat(repository.buscarPorId(id).orElseThrow().accesos()).isEmpty();
    }

    @Test
    void asignarAUnUsuarioInexistenteLanzaExcepcionAntesDeConsultarLaEmpresa() {
        assertThatThrownBy(() -> asignarEmpresaUsuario.ejecutar(99L, 1L, "GESTOR"))
                .isInstanceOf(UsuarioNoEncontradoException.class);
        assertThat(empresasConsultadas).isEmpty();
    }

    @Test
    void asignarRechazaUnRolInvalido() {
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> asignarEmpresaUsuario.ejecutar(id, 1L, "JEFE"))
                .isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void retirarQuitaSoloElAccesoIndicado() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR), new AccesoEmpresa(2L, Rol.CONSULTA));

        retirarEmpresaUsuario.ejecutar(id, 1L);

        assertThat(repository.buscarPorId(id).orElseThrow().accesos()).containsExactly(new AccesoEmpresa(2L, Rol.CONSULTA));
    }

    @Test
    void retirarUnAccesoInexistenteLanzaExcepcion() {
        Long id = guardado("ana@empresa.es", true, new AccesoEmpresa(1L, Rol.GESTOR));

        assertThatThrownBy(() -> retirarEmpresaUsuario.ejecutar(id, 2L))
                .isInstanceOf(AccesoEmpresaNoEncontradoException.class);
    }

    @Test
    void retirarDeUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> retirarEmpresaUsuario.ejecutar(99L, 1L))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void buscarDevuelveVacioSiNoExiste() {
        Long id = guardado("ana@empresa.es", true);

        assertThat(buscarUsuario.ejecutar(id)).map(UsuarioDto::email).contains("ana@empresa.es");
        assertThat(buscarUsuario.ejecutar(99L)).isEmpty();
    }

    @Test
    void buscarPorEmailNoDistingueMayusculas() {
        guardado("ana@empresa.es", true);

        assertThat(buscarUsuarioPorEmail.ejecutar("ANA@Empresa.ES")).map(UsuarioDto::email).contains("ana@empresa.es");
        assertThat(buscarUsuarioPorEmail.ejecutar("otro@empresa.es")).isEmpty();
        assertThat(buscarUsuarioPorEmail.ejecutar(null)).isEmpty();
    }

    @Test
    void verificarCredencialesCorrectasDevuelveElUsuario() {
        Long id = guardado("ana@empresa.es", true);

        assertThat(verificarCredenciales.ejecutar("Ana@empresa.es", "secreta123")).map(UsuarioDto::id).contains(id);
    }

    @Test
    void verificarCredencialesDevuelveVacioSiLaContrasenaNoCoincide() {
        guardado("ana@empresa.es", true);

        assertThat(verificarCredenciales.ejecutar("ana@empresa.es", "otra12345")).isEmpty();
        assertThat(verificarCredenciales.ejecutar("ana@empresa.es", null)).isEmpty();
    }

    @Test
    void verificarCredencialesDevuelveVacioSiElUsuarioNoExisteOEstaInactivo() {
        guardado("inactivo@empresa.es", false);

        assertThat(verificarCredenciales.ejecutar("inactivo@empresa.es", "secreta123")).isEmpty();
        assertThat(verificarCredenciales.ejecutar("nadie@empresa.es", "secreta123")).isEmpty();
        assertThat(verificarCredenciales.ejecutar(null, "secreta123")).isEmpty();
    }

    @Test
    void cambiarPasswordGuardaElNuevoHash() {
        Long id = guardado("ana@empresa.es", true);

        cambiarPassword.ejecutar(id, "nuevaClave1");

        assertThat(repository.buscarPorId(id)).map(Usuario::passwordHash).contains("hash:nuevaClave1");
        assertThat(verificarCredenciales.ejecutar("ana@empresa.es", "nuevaClave1")).isPresent();
        assertThat(verificarCredenciales.ejecutar("ana@empresa.es", "secreta123")).isEmpty();
    }

    @Test
    void cambiarPasswordAplicaLasReglasDeLongitud() {
        Long id = guardado("ana@empresa.es", true);

        assertThatThrownBy(() -> cambiarPassword.ejecutar(id, "1234567"))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La contraseña debe tener entre 8 y 72 caracteres");
        assertThatThrownBy(() -> cambiarPassword.ejecutar(id, "a".repeat(73))).isInstanceOf(UsuarioInvalidoException.class);
        assertThat(repository.buscarPorId(id)).map(Usuario::passwordHash).contains("hash:secreta123");
    }

    @Test
    void cambiarPasswordDeUnUsuarioInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> cambiarPassword.ejecutar(99L, "nuevaClave1"))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }
}
