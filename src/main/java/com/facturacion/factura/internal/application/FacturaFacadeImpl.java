package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class FacturaFacadeImpl implements FacturaFacade {

    private final CrearFactura crearFactura;
    private final ObtenerFactura obtenerFactura;
    private final ListarFacturas listarFacturas;
    private final EliminarFactura eliminarFactura;

    FacturaFacadeImpl(
            CrearFactura crearFactura,
            ObtenerFactura obtenerFactura,
            ListarFacturas listarFacturas,
            EliminarFactura eliminarFactura) {
        this.crearFactura = crearFactura;
        this.obtenerFactura = obtenerFactura;
        this.listarFacturas = listarFacturas;
        this.eliminarFactura = eliminarFactura;
    }

    @Override
    public FacturaDto crear(NuevaFacturaDto nuevaFactura) {
        return crearFactura.ejecutar(nuevaFactura);
    }

    @Override
    public FacturaDto obtener(Long id) {
        return obtenerFactura.ejecutar(id);
    }

    @Override
    public List<FacturaDto> listar() {
        return listarFacturas.ejecutar();
    }

    @Override
    public void eliminar(Long id) {
        eliminarFactura.ejecutar(id);
    }
}
