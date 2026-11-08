package com.facturacion.pagos.api.http.controllers;

import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.NuevoPagoDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.api.http.dto.EstadoFacturaResponse;
import com.facturacion.pagos.api.http.dto.PagoResponse;
import com.facturacion.pagos.api.http.dto.RegistrarPagoRequest;
import com.facturacion.pagos.internal.application.AnularPago;
import com.facturacion.pagos.internal.application.ConsultarEstadoFacturas;
import com.facturacion.pagos.internal.application.ListarPagos;
import com.facturacion.pagos.internal.application.ObtenerPago;
import com.facturacion.pagos.internal.application.RegistrarPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final RegistrarPago registrarPago;
    private final ObtenerPago obtenerPago;
    private final ListarPagos listarPagos;
    private final ConsultarEstadoFacturas consultarEstadoFacturas;
    private final AnularPago anularPago;

    public PagoController(
            RegistrarPago registrarPago,
            ObtenerPago obtenerPago,
            ListarPagos listarPagos,
            ConsultarEstadoFacturas consultarEstadoFacturas,
            AnularPago anularPago) {
        this.registrarPago = registrarPago;
        this.obtenerPago = obtenerPago;
        this.listarPagos = listarPagos;
        this.consultarEstadoFacturas = consultarEstadoFacturas;
        this.anularPago = anularPago;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> registrar(@Valid @RequestBody RegistrarPagoRequest request) {
        PagoDto registrado = registrarPago.ejecutar(aNuevoPago(request));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registrado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(aRespuesta(registrado));
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return aRespuesta(obtenerPago.ejecutar(id));
    }

    @GetMapping
    public List<PagoResponse> listar(
            @RequestParam(required = false) Long facturaId,
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) @Pattern(regexp = "COBRO|PAGO") String tipo) {
        return listarPagos.ejecutar(facturaId, empresaId, tipo).stream().map(PagoController::aRespuesta).toList();
    }

    @GetMapping("/estado-facturas")
    public List<EstadoFacturaResponse> consultarEstadoFacturas(
            @RequestParam Long empresaId,
            @RequestParam(required = false) @Pattern(regexp = "PENDIENTE|PARCIAL|PAGADA|VENCIDA") String estado) {
        return consultarEstadoFacturas.ejecutar(empresaId, estado).stream()
                .map(PagoController::aRespuesta)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        anularPago.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    private static NuevoPagoDto aNuevoPago(RegistrarPagoRequest request) {
        return new NuevoPagoDto(
                request.facturaId(),
                request.fecha(),
                request.importe(),
                request.metodo(),
                request.referencia());
    }

    private static PagoResponse aRespuesta(PagoDto pago) {
        return new PagoResponse(
                pago.id(),
                pago.facturaId(),
                pago.empresaId(),
                pago.tipo(),
                pago.fecha(),
                pago.importe(),
                pago.metodo(),
                pago.referencia());
    }

    private static EstadoFacturaResponse aRespuesta(EstadoFacturaDto estado) {
        return new EstadoFacturaResponse(
                estado.facturaId(),
                estado.numero(),
                estado.tipo(),
                estado.fecha(),
                estado.fechaVencimiento(),
                estado.total(),
                estado.pagado(),
                estado.pendiente(),
                estado.estado(),
                estado.vencida());
    }
}
