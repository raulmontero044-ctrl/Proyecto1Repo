package com.facturacion.documentos.api.http.controllers;

import com.facturacion.documentos.internal.application.AdjuntarDocumento;
import com.facturacion.documentos.internal.application.DescargarDocumento;
import com.facturacion.documentos.internal.application.DocumentoDto;
import com.facturacion.documentos.internal.application.EliminarDocumento;
import com.facturacion.documentos.internal.application.GenerarPdfFactura;
import com.facturacion.documentos.internal.application.ListarDocumentos;
import com.facturacion.documentos.internal.application.NuevoDocumentoDto;
import com.facturacion.documentos.internal.application.ObtenerDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
class DocumentoControllerJsonTest {

    private static final Instant CREADO_EN = Instant.parse("2026-11-08T11:00:00Z");

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

    @Test
    void generarPdfDevuelveElJsonCompletoDelDocumento() throws Exception {
        when(generarPdfFactura.ejecutar(10L)).thenReturn(new DocumentoDto(
                5L, "FACTURA", 10L, "factura-F-1.pdf", "application/pdf", 1234, "GENERADO", CREADO_EN));

        mockMvc.perform(post("/api/documentos/facturas/10/pdf"))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "id": 5,
                          "entidadTipo": "FACTURA",
                          "entidadId": 10,
                          "nombre": "factura-F-1.pdf",
                          "contentType": "application/pdf",
                          "tamano": 1234,
                          "origen": "GENERADO",
                          "creadoEn": "2026-11-08T11:00:00Z"
                        }
                        """, true));
    }

    @Test
    void adjuntarDevuelveElJsonCompletoDelDocumento() throws Exception {
        when(adjuntarDocumento.ejecutar(any(NuevoDocumentoDto.class))).thenReturn(new DocumentoDto(
                7L, "PAGO", 30L, "recibo.jpg", "image/jpeg", 3, "ADJUNTO", CREADO_EN));

        mockMvc.perform(multipart("/api/documentos")
                        .file(new MockMultipartFile("archivo", "recibo.jpg", "image/jpeg", new byte[] {1, 2, 3}))
                        .param("entidadTipo", "PAGO")
                        .param("entidadId", "30"))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "id": 7,
                          "entidadTipo": "PAGO",
                          "entidadId": 30,
                          "nombre": "recibo.jpg",
                          "contentType": "image/jpeg",
                          "tamano": 3,
                          "origen": "ADJUNTO",
                          "creadoEn": "2026-11-08T11:00:00Z"
                        }
                        """, true));
    }

    @Test
    void listarDevuelveSoloLosMetadatosDeCadaDocumento() throws Exception {
        when(listarDocumentos.ejecutar("TERCERO", 20L)).thenReturn(List.of(new DocumentoDto(
                1L, "TERCERO", 20L, "dni.png", "image/png", 512, "ADJUNTO", CREADO_EN)));

        mockMvc.perform(get("/api/documentos").param("entidadTipo", "TERCERO").param("entidadId", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": 1,
                            "entidadTipo": "TERCERO",
                            "entidadId": 20,
                            "nombre": "dni.png",
                            "contentType": "image/png",
                            "tamano": 512,
                            "origen": "ADJUNTO",
                            "creadoEn": "2026-11-08T11:00:00Z"
                          }
                        ]
                        """, true));
    }

    @Test
    void obtenerDevuelveElJsonCompletoDelDocumento() throws Exception {
        when(obtenerDocumento.ejecutar(1L)).thenReturn(new DocumentoDto(
                1L, "EMPRESA", 3L, "logo.png", "image/png", 512, "ADJUNTO", CREADO_EN));

        mockMvc.perform(get("/api/documentos/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "entidadTipo": "EMPRESA",
                          "entidadId": 3,
                          "nombre": "logo.png",
                          "contentType": "image/png",
                          "tamano": 512,
                          "origen": "ADJUNTO",
                          "creadoEn": "2026-11-08T11:00:00Z"
                        }
                        """, true));
    }
}
