package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.AccesoEmpresaNoEncontradoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    private static final String HASH = "$2a$10$abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012";

    private static Usuario usuario(List<AccesoEmpresa> accesos) {
        return new Usuario(7L, "ana@empresa.es", "Ana", HASH, true, accesos);
    }

    private static AccesoEmpresa acceso(Long empresaId, Rol rol) {
        return new AccesoEmpresa(empresaId, rol);
    }

    @Test
    void nuevoCreaUnUsuarioActivoSinIdentificador() {
        Usuario usuario = Usuario.nuevo("ana@empresa.es", "Ana", HASH, List.of(acceso(1L, Rol.GESTOR)));

        assertThat(usuario.id()).isNull();
        assertThat(usuario.activo()).isTrue();
        assertThat(usuario.email()).isEqualTo("ana@empresa.es");
        assertThat(usuario.nombre()).isEqualTo("Ana");
        assertThat(usuario.passwordHash()).isEqualTo(HASH);
        assertThat(usuario.accesos()).containsExactly(acceso(1L, Rol.GESTOR));
    }

    @Test
    void normalizaElEmailAMinusculas() {
        Usuario usuario = Usuario.nuevo("Ana.Perez@Empresa.ES", "Ana", HASH, List.of());

        assertThat(usuario.email()).isEqualTo("ana.perez@empresa.es");
    }

    @Test
    void normalizarEmailNuloDevuelveNulo() {
        assertThat(Usuario.normalizarEmail(null)).isNull();
        assertThat(Usuario.normalizarEmail("ANA@EMPRESA.ES")).isEqualTo("ana@empresa.es");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rechazaEmailVacio(String email) {
        assertThatThrownBy(() -> Usuario.nuevo(email, "Ana", HASH, List.of()))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El email del usuario es obligatorio");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rechazaNombreVacio(String nombre) {
        assertThatThrownBy(() -> Usuario.nuevo("ana@empresa.es", nombre, HASH, List.of()))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El nombre del usuario es obligatorio");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rechazaPasswordHashVacio(String hash) {
        assertThatThrownBy(() -> Usuario.nuevo("ana@empresa.es", "Ana", hash, List.of()))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La contraseña cifrada del usuario es obligatoria");
    }

    @Test
    void rechazaAccesosNulos() {
        assertThatThrownBy(() -> Usuario.nuevo("ana@empresa.es", "Ana", HASH, null))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("Los accesos del usuario son obligatorios");
    }

    @Test
    void rechazaUnAccesoNulo() {
        List<AccesoEmpresa> accesos = Arrays.asList(acceso(1L, Rol.GESTOR), null);

        assertThatThrownBy(() -> Usuario.nuevo("ana@empresa.es", "Ana", HASH, accesos))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("Los accesos del usuario no pueden ser nulos");
    }

    @Test
    void rechazaDosAccesosALaMismaEmpresa() {
        List<AccesoEmpresa> accesos = List.of(acceso(1L, Rol.GESTOR), acceso(1L, Rol.CONSULTA));

        assertThatThrownBy(() -> Usuario.nuevo("ana@empresa.es", "Ana", HASH, accesos))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El usuario solo puede tener un acceso por empresa");
    }

    @Test
    void aceptaAccesosAEmpresasDistintas() {
        Usuario usuario = usuario(List.of(acceso(1L, Rol.GESTOR), acceso(2L, Rol.CONSULTA)));

        assertThat(usuario.accesos()).extracting(AccesoEmpresa::empresaId).containsExactly(1L, 2L);
    }

    @Test
    void copiaLosAccesosDeFormaInmutable() {
        List<AccesoEmpresa> accesos = new ArrayList<>(List.of(acceso(1L, Rol.GESTOR)));
        Usuario usuario = usuario(accesos);

        accesos.add(acceso(2L, Rol.CONSULTA));

        assertThat(usuario.accesos()).hasSize(1);
        assertThatThrownBy(() -> usuario.accesos().add(acceso(3L, Rol.GESTOR)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void conDatosCambiaNombreYEmailNormalizadoYConservaElResto() {
        Usuario original = usuario(List.of(acceso(1L, Rol.GESTOR)));

        Usuario modificado = original.conDatos("Ana María", "ANA.MARIA@empresa.es");

        assertThat(modificado).isEqualTo(new Usuario(
                7L, "ana.maria@empresa.es", "Ana María", HASH, true, List.of(acceso(1L, Rol.GESTOR))));
    }

    @Test
    void conDatosAplicaLasInvariantes() {
        Usuario original = usuario(List.of());

        assertThatThrownBy(() -> original.conDatos(" ", "ana@empresa.es"))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El nombre del usuario es obligatorio");
    }

    @Test
    void conPasswordHashSoloCambiaElHash() {
        Usuario modificado = usuario(List.of()).conPasswordHash("otroHash");

        assertThat(modificado).isEqualTo(new Usuario(7L, "ana@empresa.es", "Ana", "otroHash", true, List.of()));
    }

    @Test
    void desactivadoMarcaElUsuarioComoInactivo() {
        Usuario desactivado = usuario(List.of(acceso(1L, Rol.GESTOR))).desactivado();

        assertThat(desactivado.activo()).isFalse();
        assertThat(desactivado.id()).isEqualTo(7L);
        assertThat(desactivado.accesos()).containsExactly(acceso(1L, Rol.GESTOR));
    }

    @Test
    void conAccesoAnadeUnaEmpresaNueva() {
        Usuario modificado = usuario(List.of(acceso(1L, Rol.GESTOR))).conAcceso(acceso(2L, Rol.CONSULTA));

        assertThat(modificado.accesos()).containsExactly(acceso(1L, Rol.GESTOR), acceso(2L, Rol.CONSULTA));
    }

    @Test
    void conAccesoCambiaElRolDeUnaEmpresaExistenteSinDuplicarla() {
        Usuario original = usuario(List.of(acceso(1L, Rol.GESTOR), acceso(2L, Rol.CONSULTA)));

        Usuario modificado = original.conAcceso(acceso(1L, Rol.ADMINISTRADOR));

        assertThat(modificado.accesos())
                .containsExactly(acceso(1L, Rol.ADMINISTRADOR), acceso(2L, Rol.CONSULTA));
    }

    @Test
    void conAccesoRechazaUnAccesoNulo() {
        Usuario original = usuario(List.of());

        assertThatThrownBy(() -> original.conAcceso(null))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("El acceso a la empresa es obligatorio");
    }

    @Test
    void sinAccesoARetiraSoloLaEmpresaIndicada() {
        Usuario original = usuario(List.of(acceso(1L, Rol.GESTOR), acceso(2L, Rol.CONSULTA)));

        Usuario modificado = original.sinAccesoA(1L);

        assertThat(modificado.accesos()).containsExactly(acceso(2L, Rol.CONSULTA));
    }

    @Test
    void sinAccesoAUnaEmpresaSinAccesoLanzaExcepcion() {
        Usuario original = usuario(List.of(acceso(1L, Rol.GESTOR)));

        assertThatThrownBy(() -> original.sinAccesoA(2L))
                .isInstanceOf(AccesoEmpresaNoEncontradoException.class)
                .hasMessage("El usuario con id 7 no tiene acceso a la empresa con id 2");
    }

    @Test
    void toStringNoIncluyeElHashDeLaContrasena() {
        String texto = usuario(List.of(acceso(1L, Rol.GESTOR))).toString();

        assertThat(texto).doesNotContain(HASH).contains("ana@empresa.es").contains("Ana");
    }
}
