package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.exception.CredencialesInvalidasException;
import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.service.FirmadorTokenAcceso;
import com.facturacion.auth.internal.domain.service.GeneradorRefreshToken;
import com.facturacion.auth.internal.domain.service.HashRefreshToken;
import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoAuthTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T10:00:00Z");
    private static final Instant CADUCIDAD_ACCESO = Instant.parse("2026-11-09T10:05:00Z");
    private static final Instant CADUCIDAD_REFRESH = Instant.parse("2026-11-19T10:00:00Z");

    private final RefreshTokenRepositoryEnMemoria repository = new RefreshTokenRepositoryEnMemoria();
    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final FirmadorTokenAcceso firmador = token -> "acceso:" + token.usuarioId() + ":"
            + token.rolEfectivo().map(Enum::name).orElse("SIN_ROL") + ":" + token.emitidoEn() + ":" + token.expiraEn();
    private int refreshGenerados;
    private final GeneradorRefreshToken generador = () -> "refresh-" + ++refreshGenerados;

    private Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);

    private EmisorSesion emisor() {
        return new EmisorSesion(repository, firmador, generador, reloj);
    }

    private IniciarSesion iniciarSesion() {
        return new IniciarSesion(usuarios, emisor());
    }

    private RenovarSesion renovarSesion() {
        return new RenovarSesion(repository, usuarios, emisor(), reloj);
    }

    private final CerrarSesion cerrarSesion = new CerrarSesion(repository);

    private static UsuarioDto usuario(Long id, boolean activo, boolean administrador, String... roles) {
        List<AccesoEmpresaDto> accesos = new ArrayList<>();
        for (int indice = 0; indice < roles.length; indice++) {
            accesos.add(new AccesoEmpresaDto((long) indice + 1, roles[indice]));
        }
        return new UsuarioDto(id, "usuario" + id + "@empresa.es", "Usuario", activo, administrador, accesos);
    }

    private void registrar(UsuarioDto usuario) {
        usuarios.registrar(usuario, "secreta123");
    }

    private void avanzarHasta(Instant instante) {
        reloj = Clock.fixed(instante, ZoneOffset.UTC);
    }

    @Test
    void iniciarSesionDevuelveElParDeTokensConLaCaducidadDelAcceso() {
        registrar(usuario(7L, true, false, "GESTOR"));

        SesionDto sesion = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123");

        assertThat(sesion).isEqualTo(new SesionDto(
                "acceso:7:GESTOR:" + AHORA + ":" + CADUCIDAD_ACCESO, "refresh-1", CADUCIDAD_ACCESO));
    }

    @Test
    void iniciarSesionGuardaSoloElHashDelRefreshConCaducidadADiezDias() {
        registrar(usuario(7L, true, false, "GESTOR"));

        iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123");

        assertThat(repository.todos()).containsExactly(
                new RefreshToken(1L, 7L, HashRefreshToken.calcular("refresh-1"), CADUCIDAD_REFRESH, false));
        assertThat(repository.todos()).noneMatch(token -> token.hash().contains("refresh-1"));
    }

    @Test
    void elTokenDeAccesoLlevaElRolEfectivoMasAlto() {
        registrar(usuario(7L, true, false, "CONSULTA", "ADMINISTRADOR", "GESTOR"));
        registrar(usuario(8L, true, false, "CONSULTA", "GESTOR"));
        registrar(usuario(9L, true, false, "CONSULTA"));

        assertThat(iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").accessToken())
                .startsWith("acceso:7:ADMINISTRADOR:");
        assertThat(iniciarSesion().ejecutar("usuario8@empresa.es", "secreta123").accessToken())
                .startsWith("acceso:8:GESTOR:");
        assertThat(iniciarSesion().ejecutar("usuario9@empresa.es", "secreta123").accessToken())
                .startsWith("acceso:9:CONSULTA:");
    }

    @Test
    void unAdministradorSinEmpresasRecibeElRolAdministrador() {
        registrar(usuario(7L, true, true));

        assertThat(iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").accessToken())
                .startsWith("acceso:7:ADMINISTRADOR:");
    }

    @Test
    void unUsuarioSinRolPuedeAutenticarseConUnTokenSinRol() {
        registrar(usuario(7L, true, false));

        SesionDto sesion = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123");

        assertThat(sesion.accessToken()).startsWith("acceso:7:SIN_ROL:");
        assertThat(sesion.refreshToken()).isEqualTo("refresh-1");
    }

    @Test
    void iniciarSesionConContrasenaIncorrectaLanzaExcepcionYNoGuardaNada() {
        registrar(usuario(7L, true, false, "GESTOR"));

        assertThatThrownBy(() -> iniciarSesion().ejecutar("usuario7@empresa.es", "otraClave1"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void iniciarSesionConEmailDesconocidoLanzaExcepcion() {
        assertThatThrownBy(() -> iniciarSesion().ejecutar("nadie@empresa.es", "secreta123"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void iniciarSesionDeUnUsuarioInactivoLanzaExcepcion() {
        registrar(usuario(7L, false, false, "GESTOR"));

        assertThatThrownBy(() -> iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void renovarDevuelveUnParNuevoYRevocaElUsado() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        Instant despues = AHORA.plusSeconds(3600);
        avanzarHasta(despues);

        SesionDto renovada = renovarSesion().ejecutar(usado);

        Instant caducidadAcceso = despues.plus(Duration.ofMinutes(5));
        assertThat(renovada).isEqualTo(new SesionDto(
                "acceso:7:GESTOR:" + despues + ":" + caducidadAcceso, "refresh-2", caducidadAcceso));
        assertThat(repository.todos()).containsExactly(
                new RefreshToken(1L, 7L, HashRefreshToken.calcular("refresh-1"), CADUCIDAD_REFRESH, true),
                new RefreshToken(2L, 7L, HashRefreshToken.calcular("refresh-2"), despues.plus(RefreshToken.DURACION), false));
    }

    @Test
    void renovarRecalculaElRolEfectivoActualDelUsuario() {
        registrar(usuario(7L, true, false, "CONSULTA"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        registrar(usuario(7L, true, false, "GESTOR"));

        assertThat(renovarSesion().ejecutar(usado).accessToken()).startsWith("acceso:7:GESTOR:");
    }

    @Test
    void reutilizarUnRefreshYaRotadoLanzaExcepcion() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        renovarSesion().ejecutar(usado);

        assertThatThrownBy(() -> renovarSesion().ejecutar(usado))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("El refresh token no es válido");
        assertThat(repository.todos()).hasSize(2);
    }

    @Test
    void renovarConUnRefreshDesconocidoLanzaExcepcion() {
        assertThatThrownBy(() -> renovarSesion().ejecutar("desconocido"))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("El refresh token no es válido");
    }

    @Test
    void renovarSinRefreshLanzaExcepcion() {
        assertThatThrownBy(() -> renovarSesion().ejecutar(null))
                .isInstanceOf(RefreshTokenInvalidoException.class);
    }

    @Test
    void renovarEnElInstanteDeCaducidadLanzaExcepcionYNoRevocaNiEmite() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        avanzarHasta(CADUCIDAD_REFRESH);

        assertThatThrownBy(() -> renovarSesion().ejecutar(usado)).isInstanceOf(RefreshTokenInvalidoException.class);
        assertThat(repository.todos()).singleElement().extracting(RefreshToken::revocado).isEqualTo(false);
    }

    @Test
    void renovarJustoAntesDeCaducarFunciona() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        avanzarHasta(CADUCIDAD_REFRESH.minusMillis(1));

        assertThat(renovarSesion().ejecutar(usado).refreshToken()).isEqualTo("refresh-2");
    }

    @Test
    void renovarParaUnUsuarioDesactivadoLanzaExcepcionYNoEmite() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        registrar(usuario(7L, false, false, "GESTOR"));

        assertThatThrownBy(() -> renovarSesion().ejecutar(usado)).isInstanceOf(RefreshTokenInvalidoException.class);
        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void renovarParaUnUsuarioQueYaNoExisteLanzaExcepcionYNoEmite() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String usado = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        usuarios.eliminar(7L);

        assertThatThrownBy(() -> renovarSesion().ejecutar(usado)).isInstanceOf(RefreshTokenInvalidoException.class);
        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void cerrarSesionRevocaElRefreshIndicado() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String refresh = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        String otro = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();

        cerrarSesion.ejecutar(refresh);

        assertThat(repository.buscarPorHash(HashRefreshToken.calcular(refresh))).map(RefreshToken::revocado).contains(true);
        assertThat(repository.buscarPorHash(HashRefreshToken.calcular(otro))).map(RefreshToken::revocado).contains(false);
    }

    @Test
    void trasCerrarSesionElRefreshNoSirveParaRenovar() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String refresh = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();

        cerrarSesion.ejecutar(refresh);

        assertThatThrownBy(() -> renovarSesion().ejecutar(refresh)).isInstanceOf(RefreshTokenInvalidoException.class);
    }

    @Test
    void cerrarSesionConUnRefreshDesconocidoNuloORevocadoNoHaceNada() {
        registrar(usuario(7L, true, false, "GESTOR"));
        String refresh = iniciarSesion().ejecutar("usuario7@empresa.es", "secreta123").refreshToken();
        cerrarSesion.ejecutar(refresh);
        List<RefreshToken> antes = repository.todos();

        cerrarSesion.ejecutar("desconocido");
        cerrarSesion.ejecutar(null);
        cerrarSesion.ejecutar(refresh);

        assertThat(repository.todos()).isEqualTo(antes);
    }
}
