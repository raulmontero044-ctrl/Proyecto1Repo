package com.facturacion.pagos.internal.persistence.mapper;

import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.persistence.entity.PagoEntity;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoEntity aEntidad(Pago pago) {
        return new PagoEntity(
                pago.id(),
                pago.facturaId(),
                pago.empresaId(),
                pago.tipo().name(),
                pago.fecha(),
                pago.importe(),
                pago.metodo().name(),
                pago.referencia());
    }

    public Pago aDominio(PagoEntity entidad) {
        return new Pago(
                entidad.getId(),
                entidad.getFacturaId(),
                entidad.getEmpresaId(),
                TipoPago.valueOf(entidad.getTipo()),
                entidad.getFecha(),
                entidad.getImporte(),
                MetodoPago.valueOf(entidad.getMetodo()),
                entidad.getReferencia());
    }
}
