package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoContenidoTest {

    @ParameterizedTest
    @CsvSource({
            "application/pdf, PDF",
            "image/png, PNG",
            "image/jpeg, JPEG",
            "IMAGE/PNG, PNG"})
    void reconoceLosTiposDeContenidoPermitidos(String mime, TipoContenido esperado) {
        assertThat(TipoContenido.desdeMime(mime)).isEqualTo(esperado);
    }

    @Test
    void cadaTipoConservaSuMime() {
        assertThat(TipoContenido.PDF.mime()).isEqualTo("application/pdf");
        assertThat(TipoContenido.PNG.mime()).isEqualTo("image/png");
        assertThat(TipoContenido.JPEG.mime()).isEqualTo("image/jpeg");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"text/plain", "image/gif", "application/octet-stream", "image/jpg", "application/pdfx"})
    void rechazaLosTiposDeContenidoNoPermitidos(String mime) {
        assertThatThrownBy(() -> TipoContenido.desdeMime(mime))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de contenido del documento debe ser application/pdf, image/png o image/jpeg");
    }
}
