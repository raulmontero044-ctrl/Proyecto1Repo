package com.facturacion.terceros.internal.domain.model;

import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerceroTest {

    private static Tercero tercero(Long empresaId, String nif, String razonSocial, boolean esCliente, boolean esProveedor) {
        return new Tercero(
                1L, empresaId, nif, razonSocial, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", esCliente, esProveedor, true);
    }

    private static Tercero valido() {
        return new Tercero(
                7L, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false, false);
    }

    @Test
    void rechazaEmpresaAusente() {
        assertThatThrownBy(() -> tercero(null, "B12345678", "Tercero SL", true, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("La empresa del tercero es obligatoria");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaNifAusente(String valor) {
        assertThatThrownBy(() -> tercero(3L, valor, "Tercero SL", true, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El NIF del tercero es obligatorio");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaRazonSocialAusente(String valor) {
        assertThatThrownBy(() -> tercero(3L, "B12345678", valor, true, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("La razón social del tercero es obligatoria");
    }

    @Test
    void rechazaUnTerceroQueNoEsClienteNiProveedor() {
        assertThatThrownBy(() -> tercero(3L, "B12345678", "Tercero SL", false, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El tercero debe ser cliente, proveedor o ambos");
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true", "true, true"})
    void aceptaClienteProveedorOAmbos(boolean esCliente, boolean esProveedor) {
        Tercero tercero = tercero(3L, "B12345678", "Tercero SL", esCliente, esProveedor);

        assertThat(tercero.esCliente()).isEqualTo(esCliente);
        assertThat(tercero.esProveedor()).isEqualTo(esProveedor);
    }

    @Test
    void aceptaLosDatosOpcionalesAusentes() {
        Tercero tercero = new Tercero(
                1L, 3L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, false, true, true);

        assertThat(tercero.direccion()).isNull();
        assertThat(tercero.codigoPostal()).isNull();
        assertThat(tercero.municipio()).isNull();
        assertThat(tercero.provincia()).isNull();
        assertThat(tercero.pais()).isNull();
        assertThat(tercero.email()).isNull();
        assertThat(tercero.telefono()).isNull();
    }

    @Test
    void nuevoCreaUnTerceroActivoSinIdentificador() {
        Tercero nuevo = Tercero.nuevo(
                3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true);

        assertThat(nuevo).isEqualTo(new Tercero(
                null, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true, true));
    }

    @Test
    void nuevoAplicaLaInvarianteDeClienteOProveedor() {
        assertThatThrownBy(() -> Tercero.nuevo(
                3L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, false, false))
                .isInstanceOf(TerceroInvalidoException.class);
    }

    @Test
    void conDatosCambiaLosDatosYConservaIdEmpresaYEstado() {
        Tercero modificado = valido().conDatos(
                "A87654321", "Otro SA", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "otro@tercero.es", "600000000", false, true);

        assertThat(modificado).isEqualTo(new Tercero(
                7L, 3L, "A87654321", "Otro SA", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "otro@tercero.es", "600000000", false, true, false));
    }

    @Test
    void conDatosAplicaLasInvariantes() {
        assertThatThrownBy(() -> valido().conDatos(
                "A87654321", "Otro SA", null, null, null, null, null, null, null, false, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El tercero debe ser cliente, proveedor o ambos");
        assertThatThrownBy(() -> valido().conDatos(
                " ", "Otro SA", null, null, null, null, null, null, null, true, false))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El NIF del tercero es obligatorio");
    }

    @Test
    void desactivadoSoloCambiaElEstado() {
        Tercero activo = new Tercero(
                7L, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true, true);

        Tercero desactivado = activo.desactivado();

        assertThat(desactivado).isEqualTo(new Tercero(
                7L, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true, false));
        assertThat(activo.activo()).isTrue();
    }
}
