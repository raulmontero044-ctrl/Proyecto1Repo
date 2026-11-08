package com.facturacion.documentos.api.http.controllers;

import com.facturacion.documentos.api.http.dto.DocumentoResponse;
import com.facturacion.documentos.internal.application.AdjuntarDocumento;
import com.facturacion.documentos.internal.application.ContenidoDocumentoDto;
import com.facturacion.documentos.internal.application.DescargarDocumento;
import com.facturacion.documentos.internal.application.DocumentoDto;
import com.facturacion.documentos.internal.application.EliminarDocumento;
import com.facturacion.documentos.internal.application.GenerarPdfFactura;
import com.facturacion.documentos.internal.application.ListarDocumentos;
import com.facturacion.documentos.internal.application.NuevoDocumentoDto;
import com.facturacion.documentos.internal.application.ObtenerDocumento;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final GenerarPdfFactura generarPdfFactura;
    private final AdjuntarDocumento adjuntarDocumento;
    private final ListarDocumentos listarDocumentos;
    private final ObtenerDocumento obtenerDocumento;
    private final DescargarDocumento descargarDocumento;
    private final EliminarDocumento eliminarDocumento;

    public DocumentoController(
            GenerarPdfFactura generarPdfFactura,
            AdjuntarDocumento adjuntarDocumento,
            ListarDocumentos listarDocumentos,
            ObtenerDocumento obtenerDocumento,
            DescargarDocumento descargarDocumento,
            EliminarDocumento eliminarDocumento) {
        this.generarPdfFactura = generarPdfFactura;
        this.adjuntarDocumento = adjuntarDocumento;
        this.listarDocumentos = listarDocumentos;
        this.obtenerDocumento = obtenerDocumento;
        this.descargarDocumento = descargarDocumento;
        this.eliminarDocumento = eliminarDocumento;
    }

    @PostMapping("/facturas/{facturaId}/pdf")
    public ResponseEntity<DocumentoResponse> generarPdfFactura(@PathVariable Long facturaId) {
        return creado(generarPdfFactura.ejecutar(facturaId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> adjuntar(
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam String entidadTipo,
            @RequestParam Long entidadId) throws IOException {
        return creado(adjuntarDocumento.ejecutar(new NuevoDocumentoDto(
                entidadTipo,
                entidadId,
                archivo.getOriginalFilename(),
                archivo.getContentType(),
                archivo.getBytes())));
    }

    @GetMapping
    public List<DocumentoResponse> listar(@RequestParam String entidadTipo, @RequestParam Long entidadId) {
        return listarDocumentos.ejecutar(entidadTipo, entidadId).stream()
                .map(DocumentoController::aRespuesta)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentoResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerDocumento.ejecutar(id));
    }

    @GetMapping("/{id}/contenido")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        ContenidoDocumentoDto descargado = descargarDocumento.ejecutar(id);
        ContentDisposition adjunto = ContentDisposition.attachment()
                .filename(descargado.nombre(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(descargado.contentType()))
                .contentLength(descargado.contenido().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, adjunto.toString())
                .body(descargado.contenido());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarDocumento.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<DocumentoResponse> creado(DocumentoDto documento) {
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/documentos/{id}")
                .buildAndExpand(documento.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(documento));
    }

    private static DocumentoResponse aRespuesta(DocumentoDto documento) {
        return new DocumentoResponse(
                documento.id(),
                documento.entidadTipo(),
                documento.entidadId(),
                documento.nombre(),
                documento.contentType(),
                documento.tamano(),
                documento.origen(),
                documento.creadoEn());
    }
}
