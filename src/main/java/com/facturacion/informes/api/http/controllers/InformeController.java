package com.facturacion.informes.api.http.controllers;

import com.facturacion.informes.api.http.dto.EstadisticaPeriodoResponse;
import com.facturacion.informes.api.http.dto.FacturaResumidaResponse;
import com.facturacion.informes.api.http.dto.IngresosGastosResponse;
import com.facturacion.informes.api.http.dto.ResumenFacturasResponse;
import com.facturacion.informes.api.http.dto.ResumenImpuestosResponse;
import com.facturacion.informes.api.http.dto.ResumenPendientesResponse;
import com.facturacion.informes.api.http.dto.ResumenTesoreriaResponse;
import com.facturacion.informes.api.http.dto.SaldoFacturaResponse;
import com.facturacion.informes.api.http.dto.TramoIvaResponse;
import com.facturacion.informes.internal.application.ConsultarEstadisticas;
import com.facturacion.informes.internal.application.ConsultarImpuestos;
import com.facturacion.informes.internal.application.ConsultarIngresosGastos;
import com.facturacion.informes.internal.application.ConsultarLibroFacturas;
import com.facturacion.informes.internal.application.ConsultarPendientes;
import com.facturacion.informes.internal.application.ConsultarTesoreria;
import com.facturacion.informes.internal.application.EstadisticaPeriodoDto;
import com.facturacion.informes.internal.application.FacturaResumidaDto;
import com.facturacion.informes.internal.application.IngresosGastosDto;
import com.facturacion.informes.internal.application.ResumenFacturasDto;
import com.facturacion.informes.internal.application.ResumenImpuestosDto;
import com.facturacion.informes.internal.application.ResumenPendientesDto;
import com.facturacion.informes.internal.application.ResumenTesoreriaDto;
import com.facturacion.informes.internal.application.SaldoFacturaDto;
import com.facturacion.informes.internal.application.TramoIvaDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final ConsultarLibroFacturas consultarLibroFacturas;
    private final ConsultarIngresosGastos consultarIngresosGastos;
    private final ConsultarImpuestos consultarImpuestos;
    private final ConsultarEstadisticas consultarEstadisticas;
    private final ConsultarPendientes consultarPendientes;
    private final ConsultarTesoreria consultarTesoreria;

    public InformeController(
            ConsultarLibroFacturas consultarLibroFacturas,
            ConsultarIngresosGastos consultarIngresosGastos,
            ConsultarImpuestos consultarImpuestos,
            ConsultarEstadisticas consultarEstadisticas,
            ConsultarPendientes consultarPendientes,
            ConsultarTesoreria consultarTesoreria) {
        this.consultarLibroFacturas = consultarLibroFacturas;
        this.consultarIngresosGastos = consultarIngresosGastos;
        this.consultarImpuestos = consultarImpuestos;
        this.consultarEstadisticas = consultarEstadisticas;
        this.consultarPendientes = consultarPendientes;
        this.consultarTesoreria = consultarTesoreria;
    }

    @GetMapping("/facturas")
    public ResumenFacturasResponse facturas(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta,
            @RequestParam String tipo) {
        return aRespuesta(consultarLibroFacturas.ejecutar(empresaId, desde, hasta, tipo));
    }

    @GetMapping("/ingresos-gastos")
    public IngresosGastosResponse ingresosGastos(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta) {
        IngresosGastosDto resultado = consultarIngresosGastos.ejecutar(empresaId, desde, hasta);
        return new IngresosGastosResponse(resultado.ingresos(), resultado.gastos(), resultado.resultado());
    }

    @GetMapping("/impuestos")
    public ResumenImpuestosResponse impuestos(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta) {
        return aRespuesta(consultarImpuestos.ejecutar(empresaId, desde, hasta));
    }

    @GetMapping("/estadisticas")
    public List<EstadisticaPeriodoResponse> estadisticas(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta,
            @RequestParam String agrupacion) {
        return consultarEstadisticas.ejecutar(empresaId, desde, hasta, agrupacion).stream()
                .map(InformeController::aRespuesta)
                .toList();
    }

    @GetMapping("/pendientes")
    public ResumenPendientesResponse pendientes(@RequestParam Long empresaId) {
        return aRespuesta(consultarPendientes.ejecutar(empresaId));
    }

    @GetMapping("/tesoreria")
    public ResumenTesoreriaResponse tesoreria(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta) {
        ResumenTesoreriaDto tesoreria = consultarTesoreria.ejecutar(empresaId, desde, hasta);
        return new ResumenTesoreriaResponse(tesoreria.cobros(), tesoreria.pagos(), tesoreria.saldoNeto());
    }

    private static ResumenFacturasResponse aRespuesta(ResumenFacturasDto resumen) {
        return new ResumenFacturasResponse(
                resumen.facturas().stream().map(InformeController::aRespuesta).toList(),
                resumen.numeroFacturas(),
                resumen.base(),
                resumen.cuotaIva(),
                resumen.total());
    }

    private static FacturaResumidaResponse aRespuesta(FacturaResumidaDto factura) {
        return new FacturaResumidaResponse(
                factura.id(),
                factura.numero(),
                factura.tipo(),
                factura.fecha(),
                factura.terceroId(),
                factura.terceroNombre(),
                factura.porcentajeIva(),
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }

    private static ResumenImpuestosResponse aRespuesta(ResumenImpuestosDto impuestos) {
        return new ResumenImpuestosResponse(
                aTramos(impuestos.repercutido()),
                impuestos.cuotaRepercutida(),
                aTramos(impuestos.soportado()),
                impuestos.cuotaSoportada(),
                impuestos.resultado());
    }

    private static List<TramoIvaResponse> aTramos(List<TramoIvaDto> tramos) {
        return tramos.stream()
                .map(tramo -> new TramoIvaResponse(tramo.porcentajeIva(), tramo.base(), tramo.cuota()))
                .toList();
    }

    private static EstadisticaPeriodoResponse aRespuesta(EstadisticaPeriodoDto estadistica) {
        return new EstadisticaPeriodoResponse(
                estadistica.periodo(),
                estadistica.numeroEmitidas(),
                estadistica.totalEmitidas(),
                estadistica.numeroRecibidas(),
                estadistica.totalRecibidas());
    }

    private static ResumenPendientesResponse aRespuesta(ResumenPendientesDto pendientes) {
        return new ResumenPendientesResponse(
                pendientes.pendienteCobro(),
                pendientes.vencidoCobro(),
                pendientes.pendientePago(),
                pendientes.vencidoPago(),
                pendientes.facturas().stream().map(InformeController::aRespuesta).toList());
    }

    private static SaldoFacturaResponse aRespuesta(SaldoFacturaDto saldo) {
        return new SaldoFacturaResponse(
                saldo.facturaId(),
                saldo.numero(),
                saldo.tipo(),
                saldo.fecha(),
                saldo.fechaVencimiento(),
                saldo.total(),
                saldo.pagado(),
                saldo.pendiente(),
                saldo.estado(),
                saldo.vencida());
    }
}
