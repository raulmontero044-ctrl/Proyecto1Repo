package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.FacturaImprimible;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoDtoConverterTest {

    private static FacturaDto facturaConTipo(String tipo) {
        return new FacturaDto(
                1L, "F-1", tipo, LocalDate.of(2026, 11, 3), null, 3L, "Empresa SL", "B1", null, 20L, "Tercero SA",
                "A1", null, new BigDecimal("21.00"), null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Test
    void aDtoCopiaTodosLosCamposConLosEnumeradosComoTextoYElMime() {
        Instant creadoEn = Instant.parse("2026-11-08T11:00:00Z");
        Documento documento = new Documento(
                4L, EntidadTipo.EMPRESA, 3L, "logo.jpg", TipoContenido.JPEG, 99, OrigenDocumento.ADJUNTO, creadoEn);

        assertThat(DocumentoDtoConverter.aDto(documento)).isEqualTo(new DocumentoDto(
                4L, "EMPRESA", 3L, "logo.jpg", "image/jpeg", 99, "ADJUNTO", creadoEn));
    }

    @Test
    void unaFacturaSinLineasSeConvierteConUnaListaVacia() {
        FacturaImprimible factura = DocumentoDtoConverter.aFacturaImprimible(facturaConTipo("RECIBIDA"));

        assertThat(factura.lineas()).isEmpty();
        assertThat(factura.emisor().nombre()).isEqualTo("Tercero SA");
    }

    @Test
    void unaFacturaDeTipoDesconocidoSeRechaza() {
        assertThatThrownBy(() -> DocumentoDtoConverter.aFacturaImprimible(facturaConTipo("PROFORMA")))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }
}
