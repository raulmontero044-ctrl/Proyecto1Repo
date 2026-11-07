package com.facturacion.empresas.internal.domain.model;

import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmpresaTest {

    private static Empresa empresa(
            String razonSocial,
            String nif,
            String direccion,
            String codigoPostal,
            String municipio,
            String provincia,
            String pais) {
        return new Empresa(
                1L, razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais,
                "info@empresa.es", "910000000", true);
    }

    private static Empresa valida() {
        return new Empresa(
                7L, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", false);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaRazonSocialAusente(String valor) {
        assertThatThrownBy(() -> empresa(valor, "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("La razón social de la empresa es obligatoria");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaNifAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", valor, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("El NIF de la empresa es obligatorio");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaDireccionAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", "B12345678", valor, "28001", "Madrid", "Madrid", "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("La dirección de la empresa es obligatoria");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaCodigoPostalAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", "B12345678", "Calle Mayor 1", valor, "Madrid", "Madrid", "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("El código postal de la empresa es obligatorio");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaMunicipioAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", "B12345678", "Calle Mayor 1", "28001", valor, "Madrid", "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("El municipio de la empresa es obligatorio");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaProvinciaAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", valor, "España"))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("La provincia de la empresa es obligatoria");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rechazaPaisAusente(String valor) {
        assertThatThrownBy(() -> empresa("Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", valor))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("El país de la empresa es obligatorio");
    }

    @Test
    void aceptaEmailYTelefonoAusentes() {
        Empresa sinContacto = new Empresa(
                1L, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                null, null, true);

        assertThat(sinContacto.email()).isNull();
        assertThat(sinContacto.telefono()).isNull();
    }

    @Test
    void nuevaCreaUnaEmpresaActivaSinIdentificador() {
        Empresa nueva = Empresa.nueva(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000");

        assertThat(nueva).isEqualTo(new Empresa(
                null, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", true));
    }

    @Test
    void nuevaAplicaLasInvariantes() {
        assertThatThrownBy(() -> Empresa.nueva(
                "Empresa SL", " ", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null))
                .isInstanceOf(EmpresaInvalidaException.class);
    }

    @Test
    void conDatosFiscalesSustituyeSoloLosDatosFiscales() {
        Empresa modificada = valida().conDatosFiscales(
                "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal");

        assertThat(modificada).isEqualTo(new Empresa(
                7L, "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "info@empresa.es", "910000000", false));
    }

    @Test
    void conDatosFiscalesAplicaLasInvariantes() {
        assertThatThrownBy(() -> valida().conDatosFiscales(
                "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", null))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessage("El país de la empresa es obligatorio");
    }

    @Test
    void conContactoSustituyeSoloElContacto() {
        Empresa modificada = valida().conContacto("nuevo@empresa.es", null);

        assertThat(modificada).isEqualTo(new Empresa(
                7L, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "nuevo@empresa.es", null, false));
    }
}
