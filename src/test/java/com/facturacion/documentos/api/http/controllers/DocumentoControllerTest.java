package com.facturacion.documentos.api.http.controllers;

import com.facturacion.documentos.internal.application.AdjuntarDocumento;
import com.facturacion.documentos.internal.application.ContenidoDocumentoDto;
import com.facturacion.documentos.internal.application.DescargarDocumento;
import com.facturacion.documentos.internal.application.DocumentoDto;
import com.facturacion.documentos.internal.application.EliminarDocumento;
import com.facturacion.documentos.internal.application.GenerarPdfFactura;
import com.facturacion.documentos.internal.application.ListarDocumentos;
import com.facturacion.documentos.internal.application.NuevoDocumentoDto;
import com.facturacion.documentos.internal.application.ObtenerDocumento;
import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import com.facturacion.documentos.internal.domain.exception.FacturaInexistenteException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
class DocumentoControllerTest {

    private static final Instant CREADO_EN = Instant.parse("2026-11-08T11:00:00Z");
    private static final byte[] PNG = {1, 2, 3};
    private static final String TIPOS_PERMITIDOS =
            "El tipo de contenido del documento debe ser application/pdf, image/png o image/jpeg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerarPdfFactura generarPdfFactura;

    @MockitoBean
    private AdjuntarDocumento adjuntarDocumento;

    @MockitoBean
    private ListarDocumentos listarDocumentos;

    @MockitoBean
    private ObtenerDocumento obtenerDocumento;

    @MockitoBean
    private DescargarDocumento descargarDocumento;

    @MockitoBean
    private EliminarDocumento eliminarDocumento;

    private static DocumentoDto documento(Long id, String entidadTipo, Long entidadId, String nombre, String origen) {
        return new DocumentoDto(id, entidadTipo, entidadId, nombre, "image/png", 3, origen, CREADO_EN);
    }

    private static MockMultipartFile archivo(String nombre, String contentType, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, contentType, contenido);
    }

    @Test
    void generarPdfDevuelve201ConLocationDelDocumentoYSusMetadatos() throws Exception {
        when(generarPdfFactura.ejecutar(10L)).thenReturn(new DocumentoDto(
                5L, "FACTURA", 10L, "factura-F-1.pdf", "application/pdf", 1234, "GENERADO", CREADO_EN));

        mockMvc.perform(post("/api/documentos/facturas/10/pdf"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/documentos/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.entidadTipo").value("FACTURA"))
                .andExpect(jsonPath("$.entidadId").value(10))
                .andExpect(jsonPath("$.nombre").value("factura-F-1.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.tamano").value(1234))
                .andExpect(jsonPath("$.origen").value("GENERADO"))
                .andExpect(jsonPath("$.creadoEn").value("2026-11-08T11:00:00Z"));
        verify(generarPdfFactura).ejecutar(10L);
    }

    @Test
    void generarPdfDeUnaFacturaInexistenteDevuelve404() throws Exception {
        when(generarPdfFactura.ejecutar(99L)).thenThrow(new FacturaInexistenteException(99L));

        mockMvc.perform(post("/api/documentos/facturas/99/pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe la factura con id 99"));
    }

    @Test
    void generarPdfConUnIdNoNumericoDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/documentos/facturas/abc/pdf"))
                .andExpect(status().isBadRequest());
        verify(generarPdfFactura, never()).ejecutar(anyLong());
    }

    @Test
    void adjuntarDevuelve201ConLocationYTrasladaElArchivoAlCasoDeUso() throws Exception {
        when(adjuntarDocumento.ejecutar(any(NuevoDocumentoDto.class)))
                .thenReturn(documento(7L, "EMPRESA", 3L, "logo.png", "ADJUNTO"));

        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("logo.png", "image/png", PNG))
                        .param("entidadTipo", "EMPRESA")
                        .param("entidadId", "3"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/documentos/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("logo.png"))
                .andExpect(jsonPath("$.origen").value("ADJUNTO"));

        ArgumentCaptor<NuevoDocumentoDto> enviado = ArgumentCaptor.forClass(NuevoDocumentoDto.class);
        verify(adjuntarDocumento).ejecutar(enviado.capture());
        assertThat(enviado.getValue().entidadTipo()).isEqualTo("EMPRESA");
        assertThat(enviado.getValue().entidadId()).isEqualTo(3L);
        assertThat(enviado.getValue().nombreArchivo()).isEqualTo("logo.png");
        assertThat(enviado.getValue().contentType()).isEqualTo("image/png");
        assertThat(enviado.getValue().contenido()).containsExactly(PNG);
    }

    @Test
    void adjuntarUnTipoDeContenidoNoPermitidoDevuelve400() throws Exception {
        when(adjuntarDocumento.ejecutar(any(NuevoDocumentoDto.class)))
                .thenThrow(new DocumentoInvalidoException(TIPOS_PERMITIDOS));

        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("nota.txt", "text/plain", PNG))
                        .param("entidadTipo", "EMPRESA")
                        .param("entidadId", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(TIPOS_PERMITIDOS));
    }

    @Test
    void adjuntarUnArchivoDemasiadoGrandeDevuelve400() throws Exception {
        when(adjuntarDocumento.ejecutar(any(NuevoDocumentoDto.class)))
                .thenThrow(new DocumentoInvalidoException("El documento no puede superar 10 MB"));

        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("grande.pdf", "application/pdf", PNG))
                        .param("entidadTipo", "FACTURA")
                        .param("entidadId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El documento no puede superar 10 MB"));
    }

    @Test
    void adjuntarAUnaEntidadInexistenteDevuelve400() throws Exception {
        when(adjuntarDocumento.ejecutar(any(NuevoDocumentoDto.class)))
                .thenThrow(new DocumentoInvalidoException("No existe la entidad PAGO con id 99"));

        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("recibo.pdf", "application/pdf", PNG))
                        .param("entidadTipo", "PAGO")
                        .param("entidadId", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No existe la entidad PAGO con id 99"));
    }

    @Test
    void adjuntarSinArchivoDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(multipart("/api/documentos")
                        .param("entidadTipo", "EMPRESA")
                        .param("entidadId", "3"))
                .andExpect(status().isBadRequest());
        verify(adjuntarDocumento, never()).ejecutar(any());
    }

    @Test
    void adjuntarSinTipoDeEntidadOSinEntidadDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("logo.png", "image/png", PNG))
                        .param("entidadId", "3"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("logo.png", "image/png", PNG))
                        .param("entidadTipo", "EMPRESA"))
                .andExpect(status().isBadRequest());
        verify(adjuntarDocumento, never()).ejecutar(any());
    }

    @Test
    void adjuntarConUnaEntidadNoNumericaDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(multipart("/api/documentos")
                        .file(archivo("logo.png", "image/png", PNG))
                        .param("entidadTipo", "EMPRESA")
                        .param("entidadId", "abc"))
                .andExpect(status().isBadRequest());
        verify(adjuntarDocumento, never()).ejecutar(any());
    }

    @Test
    void listarDevuelve200ConLosMetadatosDeLaEntidad() throws Exception {
        when(listarDocumentos.ejecutar("FACTURA", 10L)).thenReturn(List.of(
                documento(1L, "FACTURA", 10L, "a.png", "ADJUNTO"),
                documento(2L, "FACTURA", 10L, "factura-F-1.pdf", "GENERADO")));

        mockMvc.perform(get("/api/documentos").param("entidadTipo", "FACTURA").param("entidadId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].origen").value("GENERADO"))
                .andExpect(jsonPath("$[0].contenido").doesNotExist());
        verify(listarDocumentos).ejecutar("FACTURA", 10L);
    }

    @Test
    void listarConUnTipoDeEntidadDesconocidoDevuelve400() throws Exception {
        String detalle = "El tipo de entidad del documento debe ser FACTURA, TERCERO, EMPRESA o PAGO";
        when(listarDocumentos.ejecutar("CLIENTE", 10L)).thenThrow(new DocumentoInvalidoException(detalle));

        mockMvc.perform(get("/api/documentos").param("entidadTipo", "CLIENTE").param("entidadId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(detalle));
    }

    @Test
    void listarSinAlgunoDeSusParametrosDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(get("/api/documentos").param("entidadTipo", "FACTURA"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/documentos").param("entidadId", "10"))
                .andExpect(status().isBadRequest());
        verify(listarDocumentos, never()).ejecutar(anyString(), anyLong());
    }

    @Test
    void obtenerDevuelve200ConLosMetadatos() throws Exception {
        when(obtenerDocumento.ejecutar(7L)).thenReturn(documento(7L, "TERCERO", 20L, "dni.png", "ADJUNTO"));

        mockMvc.perform(get("/api/documentos/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.entidadTipo").value("TERCERO"))
                .andExpect(jsonPath("$.nombre").value("dni.png"));
    }

    @Test
    void obtenerUnDocumentoInexistenteDevuelve404() throws Exception {
        when(obtenerDocumento.ejecutar(99L)).thenThrow(new DocumentoNoEncontradoException(99L));

        mockMvc.perform(get("/api/documentos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el documento con id 99"));
    }

    @Test
    void obtenerConUnIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/documentos/abc"))
                .andExpect(status().isBadRequest());
        verify(obtenerDocumento, never()).ejecutar(anyLong());
    }

    @Test
    void descargarDevuelveElBinarioConSuTipoYComoAdjunto() throws Exception {
        when(descargarDocumento.ejecutar(7L)).thenReturn(new ContenidoDocumentoDto("logo.png", "image/png", PNG));

        mockMvc.perform(get("/api/documentos/7/contenido"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Content-Length", "3"))
                .andExpect(header().string(
                        "Content-Disposition", "attachment; filename=\"=?UTF-8?Q?logo.png?=\"; filename*=UTF-8''logo.png"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void descargarCodificaLosNombresNoAsciiEnLaCabecera() throws Exception {
        when(descargarDocumento.ejecutar(7L))
                .thenReturn(new ContenidoDocumentoDto("factura ñ.pdf", "application/pdf", PNG));

        mockMvc.perform(get("/api/documentos/7/contenido"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?factura_=C3=B1.pdf?=\"; filename*=UTF-8''factura%20%C3%B1.pdf"));
    }

    @Test
    void descargarCodificaLasComillasDelNombreEnLaCabecera() throws Exception {
        when(descargarDocumento.ejecutar(7L)).thenReturn(new ContenidoDocumentoDto("a\"b.pdf", "application/pdf", PNG));

        mockMvc.perform(get("/api/documentos/7/contenido"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?a=22b.pdf?=\"; filename*=UTF-8''a%22b.pdf"));
    }

    @Test
    void descargarNoPermiteInyectarCabecerasConSaltosDeLineaEnElNombre() throws Exception {
        when(descargarDocumento.ejecutar(7L))
                .thenReturn(new ContenidoDocumentoDto("a\r\nX-Inyectada: 1.pdf", "application/pdf", PNG));

        String cabecera = mockMvc.perform(get("/api/documentos/7/contenido"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Inyectada"))
                .andReturn().getResponse().getHeader("Content-Disposition");

        assertThat(cabecera).doesNotContain("\r", "\n").endsWith("filename*=UTF-8''a%0D%0AX-Inyectada%3A%201.pdf");
    }

    @Test
    void descargarUnDocumentoInexistenteDevuelve404() throws Exception {
        when(descargarDocumento.ejecutar(99L)).thenThrow(new DocumentoNoEncontradoException(99L));

        mockMvc.perform(get("/api/documentos/99/contenido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el documento con id 99"));
    }

    @Test
    void eliminarDevuelve204SinCuerpo() throws Exception {
        mockMvc.perform(delete("/api/documentos/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(eliminarDocumento).ejecutar(7L);
    }

    @Test
    void eliminarUnDocumentoInexistenteDevuelve404() throws Exception {
        doThrow(new DocumentoNoEncontradoException(99L)).when(eliminarDocumento).ejecutar(99L);

        mockMvc.perform(delete("/api/documentos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el documento con id 99"));
    }
}
