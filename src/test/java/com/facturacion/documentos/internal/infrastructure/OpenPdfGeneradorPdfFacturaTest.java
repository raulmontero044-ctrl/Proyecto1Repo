package com.facturacion.documentos.internal.infrastructure;

import com.facturacion.documentos.internal.domain.model.FacturaImprimible;
import com.facturacion.documentos.internal.domain.model.Interviniente;
import com.facturacion.documentos.internal.domain.model.LineaImprimible;
import com.facturacion.documentos.internal.domain.model.TipoFactura;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfGeneradorPdfFacturaTest {

    private static final Interviniente EMPRESA = new Interviniente("Empresa Ñandú SL", "B00000003", "Calle Mayor 1");
    private static final Interviniente TERCERO = new Interviniente("Tercero SA", "A00000020", null);

    private final OpenPdfGeneradorPdfFactura generador = new OpenPdfGeneradorPdfFactura();

    private static FacturaImprimible factura(TipoFactura tipo) {
        return new FacturaImprimible(
                "F-2026-001",
                tipo,
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 12, 3),
                EMPRESA,
                TERCERO,
                new BigDecimal("21.00"),
                List.of(
                        new LineaImprimible("Consultoría", new BigDecimal("2.000"), new BigDecimal("50.00"),
                                new BigDecimal("100.00")),
                        new LineaImprimible("Desplazamiento", new BigDecimal("1.000"), new BigDecimal("0.10"),
                                new BigDecimal("0.10"))),
                new BigDecimal("100.10"),
                new BigDecimal("21.02"),
                new BigDecimal("121.13"));
    }

    private static String texto(byte[] pdf) throws IOException {
        PdfReader lector = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina)).append('\n');
            }
            return texto.toString();
        } finally {
            lector.close();
        }
    }

    @Test
    void generaUnDocumentoPdf() {
        byte[] pdf = generador.generar(factura(TipoFactura.EMITIDA));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void incluyeElNumeroYLasFechasDeLaFactura() throws IOException {
        String texto = texto(generador.generar(factura(TipoFactura.EMITIDA)));

        assertThat(texto)
                .contains("Factura F-2026-001")
                .contains("Fecha: 2026-11-03")
                .contains("Vencimiento: 2026-12-03");
    }

    @Test
    void enUnaFacturaEmitidaLaEmpresaEsElEmisorYElTerceroElReceptor() throws IOException {
        String texto = texto(generador.generar(factura(TipoFactura.EMITIDA)));

        assertThat(texto).containsSubsequence(
                "Emisor", "Empresa Ñandú SL", "NIF: B00000003", "Dirección: Calle Mayor 1",
                "Receptor", "Tercero SA", "NIF: A00000020", "Dirección: -");
    }

    @Test
    void enUnaFacturaRecibidaElTerceroEsElEmisorYLaEmpresaElReceptor() throws IOException {
        String texto = texto(generador.generar(factura(TipoFactura.RECIBIDA)));

        assertThat(texto).containsSubsequence(
                "Emisor", "Tercero SA", "NIF: A00000020",
                "Receptor", "Empresa Ñandú SL", "NIF: B00000003");
    }

    @Test
    void incluyeLasLineasConSusValoresTalCualVienen() throws IOException {
        String texto = texto(generador.generar(factura(TipoFactura.EMITIDA)));

        assertThat(texto).containsSubsequence(
                "Concepto", "Cantidad", "Precio unitario", "Importe",
                "Consultoría", "2.000", "50.00", "100.00",
                "Desplazamiento", "1.000", "0.10", "0.10");
    }

    @Test
    void incluyeLosTotalesTalCualVienenSinRecalcularlos() throws IOException {
        String texto = texto(generador.generar(factura(TipoFactura.EMITIDA)));

        assertThat(texto)
                .contains("Base imponible: 100.10")
                .contains("IVA (21.00 %): 21.02")
                .contains("Total: 121.13");
    }

    @Test
    void unaFacturaSinVencimientoNiLineasTambienSeGenera() throws IOException {
        FacturaImprimible sinLineas = new FacturaImprimible(
                "F-2", TipoFactura.EMITIDA, LocalDate.of(2026, 11, 3), null, EMPRESA, TERCERO,
                new BigDecimal("0.00"), List.of(), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"));

        String texto = texto(generador.generar(sinLineas));

        assertThat(texto).contains("Factura F-2").contains("Vencimiento: -").contains("Total: 0.00");
    }
}
