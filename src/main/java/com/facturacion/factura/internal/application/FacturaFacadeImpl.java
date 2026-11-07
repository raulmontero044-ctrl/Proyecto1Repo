package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class FacturaFacadeImpl implements FacturaFacade {

    private final CrearFactura crearFactura;
    private final ObtenerFactura obtenerFactura;
    private final ListarFacturas listarFacturas;
    private final EliminarFactura eliminarFactura;
    private final BuscarFactura buscarFactura;

    FacturaFacadeImpl(
            CrearFactura crearFactura,
            ObtenerFactura obtenerFactura,
            ListarFacturas listarFacturas,
            EliminarFactura eliminarFactura,
            BuscarFactura buscarFactura) {
        this.crearFactura = crearFactura;
        this.obtenerFactura = obtenerFactura;
        this.listarFacturas = listarFacturas;
        this.eliminarFactura = eliminarFactura;
        this.buscarFactura = buscarFactura;
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

    @Override
    public Optional<FacturaDto> buscar(Long id) {
        return buscarFactura.ejecutar(id);
    }
}
