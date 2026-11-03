package com.facturacion.factura.persistence.mapper;

import com.facturacion.factura.domain.model.Factura;
import com.facturacion.factura.domain.model.LineaFactura;
import com.facturacion.factura.persistence.entity.FacturaEntity;
import com.facturacion.factura.persistence.entity.LineaFacturaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FacturaMapper {

    public FacturaEntity aEntidad(Factura factura) {
        FacturaEntity entidad = new FacturaEntity(
                factura.id(),
                factura.numero(),
                factura.fecha(),
                factura.clienteNombre(),
                factura.clienteNif(),
                factura.porcentajeIva());
        List<LineaFactura> lineas = factura.lineas();
        for (int posicion = 0; posicion < lineas.size(); posicion++) {
            LineaFactura linea = lineas.get(posicion);
            entidad.agregarLinea(new LineaFacturaEntity(
                    posicion,
                    linea.concepto(),
                    linea.cantidad(),
                    linea.precioUnitario()));
        }
        return entidad;
    }

    public Factura aDominio(FacturaEntity entidad) {
        List<LineaFactura> lineas = entidad.getLineas().stream()
                .map(linea -> new LineaFactura(
                        linea.getConcepto(),
                        linea.getCantidad(),
                        linea.getPrecioUnitario()))
                .toList();
        return new Factura(
                entidad.getId(),
                entidad.getNumero(),
                entidad.getFecha(),
                entidad.getClienteNombre(),
                entidad.getClienteNif(),
                entidad.getPorcentajeIva(),
                lineas);
    }
}
