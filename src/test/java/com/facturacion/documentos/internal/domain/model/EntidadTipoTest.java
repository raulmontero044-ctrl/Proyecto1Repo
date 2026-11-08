package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntidadTipoTest {

    @ParameterizedTest
    @EnumSource(EntidadTipo.class)
    void reconoceCadaTipoDeEntidadPorSuNombre(EntidadTipo tipo) {
        assertThat(EntidadTipo.desde(tipo.name())).isEqualTo(tipo);
    }

    @Test
    void losTiposDeEntidadSonFacturaTerceroEmpresaYPago() {
        assertThat(EntidadTipo.values())
                .containsExactly(EntidadTipo.FACTURA, EntidadTipo.TERCERO, EntidadTipo.EMPRESA, EntidadTipo.PAGO);
    }

    @Test
    void rechazaUnTipoDeEntidadAusente() {
        assertThatThrownBy(() -> EntidadTipo.desde(null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de entidad del documento es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "factura", "CLIENTE"})
    void rechazaUnTipoDeEntidadDesconocido(String valor) {
        assertThatThrownBy(() -> EntidadTipo.desde(valor))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de entidad del documento debe ser FACTURA, TERCERO, EMPRESA o PAGO");
    }
}
