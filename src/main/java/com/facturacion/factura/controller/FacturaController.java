package com.facturacion.factura.controller;

import com.facturacion.factura.api.dto.CrearFacturaRequest;
import com.facturacion.factura.api.dto.FacturaDto;
import com.facturacion.factura.api.dto.NuevaFacturaDto;
import com.facturacion.factura.api.dto.NuevaLineaFacturaDto;
import com.facturacion.factura.apln.CrearFactura;
import com.facturacion.factura.apln.EliminarFactura;
import com.facturacion.factura.apln.ListarFacturas;
import com.facturacion.factura.apln.ObtenerFactura;
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
    public ResponseEntity<FacturaDto> crear(@Valid @RequestBody CrearFacturaRequest request) {
        FacturaDto creada = crearFactura.ejecutar(aNuevaFactura(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping("/{id}")
    public FacturaDto obtener(@PathVariable Long id) {
        return obtenerFactura.ejecutar(id);
    }

    @GetMapping
    public List<FacturaDto> listar() {
        return listarFacturas.ejecutar();
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
                request.fecha(),
                request.clienteNombre(),
                request.clienteNif(),
                request.porcentajeIva(),
                lineas);
    }
}
