package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

    private static final Instant CREADO_EN = Instant.parse("2026-11-08T11:00:00Z");
    private static final long DIEZ_MB = 10L * 1024 * 1024;

    private static Documento documento(String nombre, long tamano) {
        return Documento.nuevo(
                EntidadTipo.FACTURA, 7L, nombre, TipoContenido.PDF, tamano, OrigenDocumento.ADJUNTO, CREADO_EN);
    }

    @Test
    void nuevoCreaUnDocumentoSinIdentificadorConTodosSusDatos() {
        Documento documento = Documento.nuevo(
                EntidadTipo.PAGO, 9L, "recibo.png", TipoContenido.PNG, 1, OrigenDocumento.GENERADO, CREADO_EN);

        assertThat(documento).isEqualTo(new Documento(
                null, EntidadTipo.PAGO, 9L, "recibo.png", TipoContenido.PNG, 1, OrigenDocumento.GENERADO,
                CREADO_EN));
    }

    @Test
    void elTamanoMaximoEsDeDiezMegabytes() {
        assertThat(Documento.TAMANO_MAXIMO).isEqualTo(10_485_760L);
    }

    @Test
    void aceptaUnTamanoDeUnByteYDeExactamenteDiezMegabytes() {
        assertThat(documento("a.pdf", 1).tamano()).isEqualTo(1);
        assertThat(documento("a.pdf", DIEZ_MB).tamano()).isEqualTo(DIEZ_MB);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rechazaUnDocumentoVacio(long tamano) {
        assertThatThrownBy(() -> documento("a.pdf", tamano))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El documento no puede estar vacío");
    }

    @Test
    void rechazaUnDocumentoDeMasDeDiezMegabytes() {
        assertThatThrownBy(() -> documento("a.pdf", DIEZ_MB + 1))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El documento no puede superar 10 MB");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaUnNombreAusenteOEnBlanco(String nombre) {
        assertThatThrownBy(() -> documento(nombre, 1))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El nombre del documento es obligatorio");
    }

    @Test
    void aceptaUnNombreDe255CaracteresYRechazaUnoDe256() {
        assertThat(documento("a".repeat(255), 1).nombre()).hasSize(255);
        assertThatThrownBy(() -> documento("a".repeat(256), 1))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El nombre del documento no puede superar 255 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a\r\nb.pdf", "a\nb.pdf", "a\"b.pdf", "a\tb.pdf"})
    void rechazaUnNombreConCaracteresDeControlOComillas(String nombre) {
        assertThatThrownBy(() -> documento(nombre, 1))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El nombre del documento no puede contener caracteres de control ni comillas");
    }

    @Test
    void rechazaLosDatosObligatoriosAusentes() {
        assertThatThrownBy(() -> Documento.nuevo(
                null, 7L, "a.pdf", TipoContenido.PDF, 1, OrigenDocumento.ADJUNTO, CREADO_EN))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de entidad del documento es obligatorio");
        assertThatThrownBy(() -> Documento.nuevo(
                EntidadTipo.FACTURA, null, "a.pdf", TipoContenido.PDF, 1, OrigenDocumento.ADJUNTO, CREADO_EN))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La entidad del documento es obligatoria");
        assertThatThrownBy(() -> Documento.nuevo(
                EntidadTipo.FACTURA, 7L, "a.pdf", null, 1, OrigenDocumento.ADJUNTO, CREADO_EN))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de contenido del documento es obligatorio");
        assertThatThrownBy(() -> Documento.nuevo(
                EntidadTipo.FACTURA, 7L, "a.pdf", TipoContenido.PDF, 1, null, CREADO_EN))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El origen del documento es obligatorio");
        assertThatThrownBy(() -> Documento.nuevo(
                EntidadTipo.FACTURA, 7L, "a.pdf", TipoContenido.PDF, 1, OrigenDocumento.ADJUNTO, null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La fecha de creación del documento es obligatoria");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "contrato.pdf|contrato.pdf",
            "C:\\Users\\ana\\foto.png|foto.png",
            "/tmp/subida/foto.jpg|foto.jpg",
            "  informe final.pdf  |informe final.pdf",
            "fact\"ura\".pdf|factura.pdf"})
    void elNombreDeUnAdjuntoQuitaLaRutaLasComillasYLosEspaciosExteriores(String original, String esperado) {
        assertThat(Documento.nombreDeArchivoAdjunto(original)).isEqualTo(esperado);
    }

    @Test
    void elNombreDeUnAdjuntoQuitaLosSaltosDeLineaYCaracteresDeControl() {
        assertThat(Documento.nombreDeArchivoAdjunto("in\r\nforme\t.pdf")).isEqualTo("informe.pdf");
    }

    @Test
    void elNombreDeUnAdjuntoNuloSigueSiendoNulo() {
        assertThat(Documento.nombreDeArchivoAdjunto(null)).isNull();
    }

    @Test
    void elNombreDelPdfDeUnaFacturaIncluyeSuNumeroSinCaracteresNoPermitidos() {
        assertThat(Documento.nombreDePdfDeFactura("F-2026/001")).isEqualTo("factura-F-2026/001.pdf");
        assertThat(Documento.nombreDePdfDeFactura("F\"1\n")).isEqualTo("factura-F1.pdf");
    }
}
