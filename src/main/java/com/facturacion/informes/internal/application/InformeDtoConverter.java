package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import com.facturacion.informes.internal.domain.model.EstadoCobro;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.PagoResumido;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.SaldoFactura;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TipoPago;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;

import java.util.List;

final class InformeDtoConverter {

    private InformeDtoConverter() {
    }

    static Long empresaObligatoria(Long empresaId) {
        if (empresaId == null) {
            throw new InformeInvalidoException("La empresa del informe es obligatoria");
        }
        return empresaId;
    }

    static List<FacturaResumida> aFacturasResumidas(List<FacturaDto> facturas) {
        return facturas.stream().map(InformeDtoConverter::aFacturaResumida).toList();
    }

    static List<SaldoFactura> aSaldosFactura(List<EstadoFacturaDto> estados) {
        return estados.stream().map(InformeDtoConverter::aSaldoFactura).toList();
    }

    static List<PagoResumido> aPagosResumidos(List<PagoDto> pagos) {
        return pagos.stream().map(InformeDtoConverter::aPagoResumido).toList();
    }

    private static FacturaResumida aFacturaResumida(FacturaDto factura) {
        return new FacturaResumida(
                factura.id(),
                factura.numero(),
                TipoFactura.desde(factura.tipo()),
                factura.fecha(),
                factura.terceroId(),
                factura.terceroNombre(),
                factura.porcentajeIva(),
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }

    private static SaldoFactura aSaldoFactura(EstadoFacturaDto estado) {
        return new SaldoFactura(
                estado.facturaId(),
                estado.numero(),
                TipoFactura.desde(estado.tipo()),
                estado.fecha(),
                estado.fechaVencimiento(),
                estado.total(),
                estado.pagado(),
                estado.pendiente(),
                EstadoCobro.desde(estado.estado()),
                estado.vencida());
    }

    private static PagoResumido aPagoResumido(PagoDto pago) {
        return new PagoResumido(pago.id(), TipoPago.desde(pago.tipo()), pago.importe());
    }

    static ResumenFacturasDto aDto(ResumenFacturas resumen) {
        return new ResumenFacturasDto(
                resumen.facturas().stream().map(InformeDtoConverter::aDto).toList(),
                resumen.numeroFacturas(),
                resumen.base(),
                resumen.cuotaIva(),
                resumen.total());
    }

    static IngresosGastosDto aDto(IngresosGastos ingresosGastos) {
        return new IngresosGastosDto(
                ingresosGastos.ingresos(), ingresosGastos.gastos(), ingresosGastos.resultado());
    }

    private static FacturaResumidaDto aDto(FacturaResumida factura) {
        return new FacturaResumidaDto(
                factura.id(),
                factura.numero(),
                factura.tipo().name(),
                factura.fecha(),
                factura.terceroId(),
                factura.terceroNombre(),
                factura.porcentajeIva(),
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }
}
