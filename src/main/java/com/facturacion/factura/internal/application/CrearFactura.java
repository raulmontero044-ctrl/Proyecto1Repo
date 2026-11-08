package com.facturacion.factura.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import com.facturacion.terceros.api.facade.TerceroFacade;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearFactura {

    private final FacturaRepository repository;
    private final EmpresaFacade empresaFacade;
    private final TerceroFacade terceroFacade;

    public CrearFactura(FacturaRepository repository, EmpresaFacade empresaFacade, TerceroFacade terceroFacade) {
        this.repository = repository;
        this.empresaFacade = empresaFacade;
        this.terceroFacade = terceroFacade;
    }

    @Transactional
    public FacturaDto ejecutar(NuevaFacturaDto nuevaFactura) {
        EmpresaDto empresa = empresaActiva(nuevaFactura.empresaId());
        TerceroDto tercero = terceroActivoDeLaEmpresa(nuevaFactura.terceroId(), empresa.id());
        Factura factura = FacturaDtoConverter.aDominio(nuevaFactura, empresa, tercero);
        exigirCondicionDelTercero(factura.tipo(), tercero);
        exigirNumeroLibre(factura);
        return FacturaDtoConverter.aDto(repository.guardar(factura));
    }

    private void exigirNumeroLibre(Factura factura) {
        boolean duplicado = factura.tipo() == TipoFactura.EMITIDA
                ? repository.existeEmitidaPorNumero(factura.empresaId(), factura.numero())
                : repository.existeRecibidaPorNumero(factura.empresaId(), factura.terceroId(), factura.numero());
        if (duplicado) {
            throw new NumeroFacturaDuplicadoException(factura.numero());
        }
    }

    private EmpresaDto empresaActiva(Long empresaId) {
        if (empresaId == null) {
            throw new FacturaInvalidaException("La empresa de la factura es obligatoria");
        }
        EmpresaDto empresa = empresaFacade.buscar(empresaId)
                .orElseThrow(() -> new FacturaInvalidaException("No existe la empresa con id " + empresaId));
        if (!empresa.activa()) {
            throw new FacturaInvalidaException("La empresa con id " + empresaId + " no está activa");
        }
        return empresa;
    }

    private TerceroDto terceroActivoDeLaEmpresa(Long terceroId, Long empresaId) {
        if (terceroId == null) {
            throw new FacturaInvalidaException("El tercero de la factura es obligatorio");
        }
        TerceroDto tercero = terceroFacade.buscar(terceroId)
                .orElseThrow(() -> new FacturaInvalidaException("No existe el tercero con id " + terceroId));
        if (!empresaId.equals(tercero.empresaId())) {
            throw new FacturaInvalidaException(
                    "El tercero con id " + terceroId + " no pertenece a la empresa con id " + empresaId);
        }
        if (!tercero.activo()) {
            throw new FacturaInvalidaException("El tercero con id " + terceroId + " no está activo");
        }
        return tercero;
    }

    private static void exigirCondicionDelTercero(TipoFactura tipo, TerceroDto tercero) {
        if (tipo == TipoFactura.EMITIDA && !tercero.esCliente()) {
            throw new FacturaInvalidaException(
                    "Una factura EMITIDA requiere un tercero cliente y el tercero con id " + tercero.id() + " no lo es");
        }
        if (tipo == TipoFactura.RECIBIDA && !tercero.esProveedor()) {
            throw new FacturaInvalidaException(
                    "Una factura RECIBIDA requiere un tercero proveedor y el tercero con id " + tercero.id() + " no lo es");
        }
    }
}
