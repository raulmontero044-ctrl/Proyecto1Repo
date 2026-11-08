package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import com.facturacion.documentos.internal.domain.exception.FacturaInexistenteException;
import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.FacturaImprimible;
import com.facturacion.documentos.internal.domain.model.Interviniente;
import com.facturacion.documentos.internal.domain.model.LineaImprimible;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.domain.model.TipoFactura;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.documentos.internal.domain.service.GeneradorPdfFactura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CasosDeUsoDocumentoTest {

    private static final Instant AHORA = Instant.parse("2026-11-08T11:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneId.of("Europe/Madrid"));
    private static final byte[] PDF = {'%', 'P', 'D', 'F'};
    private static final byte[] PNG = {1, 2, 3};
    private static final int DIEZ_MB = 10 * 1024 * 1024;

    private final DocumentoRepositoryEnMemoria repository = new DocumentoRepositoryEnMemoria();
    private final EntidadesEnMemoria entidades = new EntidadesEnMemoria();
    private final List<FacturaImprimible> facturasImpresas = new ArrayList<>();
    private final GeneradorPdfFactura generador = factura -> {
        facturasImpresas.add(factura);
        return PDF.clone();
    };

    private final GenerarPdfFactura generarPdfFactura =
            new GenerarPdfFactura(repository, entidades.facturaFacade(), generador, RELOJ);
    private final AdjuntarDocumento adjuntarDocumento = new AdjuntarDocumento(
            repository,
            entidades.facturaFacade(),
            entidades.empresaFacade(),
            entidades.terceroFacade(),
            entidades.pagoFacade(),
            RELOJ);
    private final ListarDocumentos listarDocumentos = new ListarDocumentos(repository);
    private final ObtenerDocumento obtenerDocumento = new ObtenerDocumento(repository);
    private final DescargarDocumento descargarDocumento = new DescargarDocumento(repository);
    private final EliminarDocumento eliminarDocumento = new EliminarDocumento(repository);

    CasosDeUsoDocumentoTest() {
        entidades.registrar(EntidadesEnMemoria.factura(10L, "F-2026-001", "EMITIDA"));
        entidades.registrar(EntidadesEnMemoria.factura(11L, "R-77", "RECIBIDA"));
        entidades.registrar(EntidadesEnMemoria.empresa(3L));
        entidades.registrar(EntidadesEnMemoria.tercero(20L));
        entidades.registrar(EntidadesEnMemoria.pago(30L));
    }

    private static NuevoDocumentoDto adjunto(String entidadTipo, Long entidadId, byte[] contenido) {
        return new NuevoDocumentoDto(entidadTipo, entidadId, "logo.png", "image/png", contenido);
    }

    private Documento guardado(EntidadTipo tipo, Long entidadId) {
        return repository.guardar(
                Documento.nuevo(tipo, entidadId, "a.pdf", TipoContenido.PDF, 3, OrigenDocumento.ADJUNTO, AHORA),
                new byte[] {7, 8, 9});
    }

    @Test
    void generarElPdfDeUnaFacturaGuardaUnDocumentoGeneradoConSuContenido() {
        DocumentoDto generado = generarPdfFactura.ejecutar(10L);

        assertThat(generado).isEqualTo(new DocumentoDto(
                1L, "FACTURA", 10L, "factura-F-2026-001.pdf", "application/pdf", PDF.length, "GENERADO", AHORA));
        assertThat(repository.contenidos().get(1L)).containsExactly(PDF);
    }

    @Test
    void generarElPdfEntregaAlGeneradorLosDatosDeLaFacturaTalCualVienen() {
        generarPdfFactura.ejecutar(10L);

        assertThat(facturasImpresas).singleElement().satisfies(factura -> {
            assertThat(factura.numero()).isEqualTo("F-2026-001");
            assertThat(factura.tipo()).isEqualTo(TipoFactura.EMITIDA);
            assertThat(factura.fecha()).isEqualTo(LocalDate.of(2026, 11, 3));
            assertThat(factura.fechaVencimiento()).isEqualTo(LocalDate.of(2026, 12, 3));
            assertThat(factura.empresa())
                    .isEqualTo(new Interviniente("Empresa SL", "B00000003", "Calle Mayor 1"));
            assertThat(factura.tercero())
                    .isEqualTo(new Interviniente("Tercero SA", "A00000020", "Avenida Diagonal 2"));
            assertThat(factura.porcentajeIva()).isEqualByComparingTo("21.00");
            assertThat(factura.lineas()).containsExactly(new LineaImprimible(
                    "Servicio", new BigDecimal("2.000"), new BigDecimal("50.00"), new BigDecimal("100.00")));
            assertThat(factura.base().toPlainString()).isEqualTo("100.00");
            assertThat(factura.cuotaIva().toPlainString()).isEqualTo("21.00");
            assertThat(factura.total().toPlainString()).isEqualTo("121.00");
        });
    }

    @Test
    void generarElPdfDeUnaFacturaRecibidaPoneAlTerceroComoEmisor() {
        generarPdfFactura.ejecutar(11L);

        assertThat(facturasImpresas).singleElement().satisfies(factura -> {
            assertThat(factura.emisor().nombre()).isEqualTo("Tercero SA");
            assertThat(factura.receptor().nombre()).isEqualTo("Empresa SL");
        });
    }

    @Test
    void regenerarElPdfDeUnaFacturaCreaUnDocumentoNuevo() {
        DocumentoDto primero = generarPdfFactura.ejecutar(10L);
        DocumentoDto segundo = generarPdfFactura.ejecutar(10L);

        assertThat(segundo.id()).isNotEqualTo(primero.id());
        assertThat(repository.listarPorEntidad(EntidadTipo.FACTURA, 10L)).hasSize(2);
    }

    @Test
    void generarElPdfDeUnaFacturaInexistenteFallaSinGuardarNada() {
        assertThatThrownBy(() -> generarPdfFactura.ejecutar(99L))
                .isInstanceOf(FacturaInexistenteException.class)
                .hasMessage("No existe la factura con id 99");
        assertThat(repository.todos()).isEmpty();
        assertThat(facturasImpresas).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"FACTURA, 10", "TERCERO, 20", "EMPRESA, 3", "PAGO, 30"})
    void adjuntarAUnaEntidadExistenteGuardaElDocumentoYSuContenido(String entidadTipo, Long entidadId) {
        DocumentoDto adjuntado = adjuntarDocumento.ejecutar(adjunto(entidadTipo, entidadId, PNG));

        assertThat(adjuntado).isEqualTo(new DocumentoDto(
                1L, entidadTipo, entidadId, "logo.png", "image/png", 3, "ADJUNTO", AHORA));
        assertThat(repository.contenidos().get(1L)).containsExactly(PNG);
    }

    @ParameterizedTest
    @EnumSource(EntidadTipo.class)
    void adjuntarAUnaEntidadInexistenteFallaSinGuardarNada(EntidadTipo entidadTipo) {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto(entidadTipo.name(), 99L, PNG)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("No existe la entidad " + entidadTipo + " con id 99");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void adjuntarUsaElNombreOriginalSaneado() {
        DocumentoDto adjuntado = adjuntarDocumento.ejecutar(new NuevoDocumentoDto(
                "EMPRESA", 3L, "C:\\fotos\\mi \"logo\"\r\n.png", "image/png", PNG));

        assertThat(adjuntado.nombre()).isEqualTo("mi logo.png");
    }

    @Test
    void adjuntarAceptaUnArchivoDeExactamenteDiezMegabytes() {
        DocumentoDto adjuntado = adjuntarDocumento.ejecutar(adjunto("EMPRESA", 3L, new byte[DIEZ_MB]));

        assertThat(adjuntado.tamano()).isEqualTo(DIEZ_MB);
    }

    @Test
    void adjuntarRechazaUnArchivoDeMasDeDiezMegabytes() {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto("EMPRESA", 3L, new byte[DIEZ_MB + 1])))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El documento no puede superar 10 MB");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void adjuntarRechazaUnArchivoVacioOSinContenido() {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto("EMPRESA", 3L, new byte[0])))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El documento no puede estar vacío");
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto("EMPRESA", 3L, null)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El documento no puede estar vacío");
        assertThat(repository.todos()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "image/gif", "application/zip"})
    void adjuntarRechazaUnTipoDeContenidoNoPermitido(String contentType) {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(
                new NuevoDocumentoDto("EMPRESA", 3L, "a.txt", contentType, PNG)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de contenido del documento debe ser application/pdf, image/png o image/jpeg");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void adjuntarRechazaUnTipoDeEntidadDesconocidoOUnaEntidadAusente() {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto("CLIENTE", 3L, PNG)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de entidad del documento debe ser FACTURA, TERCERO, EMPRESA o PAGO");
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(adjunto("EMPRESA", null, PNG)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La entidad del documento es obligatoria");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void adjuntarRechazaUnArchivoSinNombre() {
        assertThatThrownBy(() -> adjuntarDocumento.ejecutar(
                new NuevoDocumentoDto("EMPRESA", 3L, "/ruta/", "image/png", PNG)))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El nombre del documento es obligatorio");
    }

    @Test
    void listarDevuelveSoloLosDocumentosDeLaEntidadIndicada() {
        Long primero = guardado(EntidadTipo.FACTURA, 10L).id();
        guardado(EntidadTipo.FACTURA, 11L);
        guardado(EntidadTipo.PAGO, 10L);
        Long segundo = guardado(EntidadTipo.FACTURA, 10L).id();

        assertThat(listarDocumentos.ejecutar("FACTURA", 10L))
                .extracting(DocumentoDto::id)
                .containsExactly(primero, segundo);
    }

    @Test
    void listarUnaEntidadSinDocumentosDevuelveUnaListaVacia() {
        assertThat(listarDocumentos.ejecutar("TERCERO", 99L)).isEmpty();
    }

    @Test
    void listarRechazaUnTipoDeEntidadDesconocidoOUnaEntidadAusente() {
        assertThatThrownBy(() -> listarDocumentos.ejecutar("CLIENTE", 10L))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de entidad del documento debe ser FACTURA, TERCERO, EMPRESA o PAGO");
        assertThatThrownBy(() -> listarDocumentos.ejecutar("FACTURA", null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La entidad del documento es obligatoria");
    }

    @Test
    void obtenerDevuelveLosMetadatosDelDocumento() {
        Long id = guardado(EntidadTipo.TERCERO, 20L).id();

        assertThat(obtenerDocumento.ejecutar(id)).isEqualTo(new DocumentoDto(
                id, "TERCERO", 20L, "a.pdf", "application/pdf", 3, "ADJUNTO", AHORA));
    }

    @Test
    void obtenerUnDocumentoInexistenteFalla() {
        assertThatThrownBy(() -> obtenerDocumento.ejecutar(99L))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessage("No existe el documento con id 99");
    }

    @Test
    void descargarDevuelveElNombreElTipoYElContenido() {
        Long id = guardado(EntidadTipo.TERCERO, 20L).id();

        ContenidoDocumentoDto descargado = descargarDocumento.ejecutar(id);

        assertThat(descargado.nombre()).isEqualTo("a.pdf");
        assertThat(descargado.contentType()).isEqualTo("application/pdf");
        assertThat(descargado.contenido()).containsExactly(7, 8, 9);
    }

    @Test
    void descargarUnDocumentoInexistenteFalla() {
        assertThatThrownBy(() -> descargarDocumento.ejecutar(99L))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessage("No existe el documento con id 99");
    }

    @Test
    void descargarUnDocumentoSinContenidoGuardadoFalla() {
        DocumentoRepository sinContenido = mock(DocumentoRepository.class);
        when(sinContenido.buscarPorId(5L)).thenReturn(Optional.of(guardado(EntidadTipo.FACTURA, 10L)));
        when(sinContenido.buscarContenido(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new DescargarDocumento(sinContenido).ejecutar(5L))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessage("No existe el documento con id 5");
    }

    @Test
    void eliminarSoloBorraElDocumentoIndicadoYSuContenido() {
        Long borrado = guardado(EntidadTipo.FACTURA, 10L).id();
        Long conservado = guardado(EntidadTipo.FACTURA, 10L).id();

        eliminarDocumento.ejecutar(borrado);

        assertThat(repository.todos()).extracting(Documento::id).containsExactly(conservado);
        assertThat(repository.contenidos()).containsOnlyKeys(conservado);
    }

    @Test
    void eliminarUnDocumentoInexistenteFallaSinBorrarNada() {
        guardado(EntidadTipo.FACTURA, 10L);

        assertThatThrownBy(() -> eliminarDocumento.ejecutar(99L))
                .isInstanceOf(DocumentoNoEncontradoException.class)
                .hasMessage("No existe el documento con id 99");
        assertThat(repository.todos()).hasSize(1);
    }
}
