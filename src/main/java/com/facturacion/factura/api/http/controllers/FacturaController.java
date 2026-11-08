package com.facturacion.factura.api.http.controllers;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaLineaFacturaDto;
import com.facturacion.factura.api.http.dto.CrearFacturaRequest;
import com.facturacion.factura.api.http.dto.FacturaResponse;
import com.facturacion.factura.api.http.dto.LineaFacturaResponse;
import com.facturacion.factura.internal.application.CrearFactura;
import com.facturacion.factura.internal.application.EliminarFactura;
import com.facturacion.factura.internal.application.ListarFacturas;
import com.facturacion.factura.internal.application.ObtenerFactura;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final CrearFactura crearFactura;
    private final ObtenerFactura obtenerFactura;
    private final ListarFacturas listarFacturas;
    private final EliminarFactura eliminarFactura;

    public FacturaController(
            CrearFactura crearFactura,
            ObtenerFactura obtenerFactura,
            ListarFacturas listarFacturas,
            EliminarFactura eliminarFactura) {
        this.crearFactura = crearFactura;
        this.obtenerFactura = obtenerFactura;
        this.listarFacturas = listarFacturas;
        this.eliminarFactura = eliminarFactura;
    }

    @PostMapping
    public ResponseEntity<FacturaResponse> crear(@Valid @RequestBody CrearFacturaRequest request) {
        FacturaDto creada = crearFactura.ejecutar(aNuevaFactura(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(creada));
    }

    @GetMapping("/{id}")
    public FacturaResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerFactura.ejecutar(id));
    }

    @GetMapping
    public List<FacturaResponse> listar() {
        return listarFacturas.ejecutar().stream().map(FacturaController::aRespuesta).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarFactura.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    private static NuevaFacturaDto aNuevaFactura(CrearFacturaRequest request) {
        List<NuevaLineaFacturaDto> lineas = request.lineas().stream()
                .map(linea -> new NuevaLineaFacturaDto(linea.concepto(), linea.cantidad(), linea.precioUnitario()))
                .toList();
        return new NuevaFacturaDto(
                request.numero(),
                request.tipo(),
                request.fecha(),
                request.fechaVencimiento(),
                request.empresaId(),
                request.terceroId(),
                request.clienteNombre(),
                request.clienteNif(),
                request.porcentajeIva(),
                lineas);
    }

    private static FacturaResponse aRespuesta(FacturaDto factura) {
        List<LineaFacturaResponse> lineas = factura.lineas().stream()
                .map(linea -> new LineaFacturaResponse(
                        linea.concepto(),
                        linea.cantidad(),
                        linea.precioUnitario(),
                        linea.importe()))
                .toList();
        return new FacturaResponse(
                factura.id(),
                factura.numero(),
                factura.tipo(),
                factura.fecha(),
                factura.fechaVencimiento(),
                factura.empresaId(),
                factura.terceroId(),
                factura.clienteNombre(),
                factura.clienteNif(),
                factura.porcentajeIva(),
                lineas,
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }
}
