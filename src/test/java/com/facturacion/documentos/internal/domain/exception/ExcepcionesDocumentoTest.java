package com.facturacion.documentos.internal.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExcepcionesDocumentoTest {

    @Test
    void documentoNoEncontradoNombraElIdentificador() {
        assertThat(new DocumentoNoEncontradoException(7L)).hasMessage("No existe el documento con id 7");
    }

    @Test
    void facturaInexistenteNombraElIdentificadorDeLaFactura() {
        assertThat(new FacturaInexistenteException(10L)).hasMessage("No existe la factura con id 10");
    }

    @Test
    void documentoInvalidoConservaElMensaje() {
        assertThat(new DocumentoInvalidoException("motivo")).hasMessage("motivo");
    }
}
