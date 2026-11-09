package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.Periodo;
import com.facturacion.informes.internal.domain.service.CalculadoraInformeFacturas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsultarIngresosGastos {

    private final FacturaFacade facturaFacade;

    public ConsultarIngresosGastos(FacturaFacade facturaFacade) {
        this.facturaFacade = facturaFacade;
    }

    @Transactional(readOnly = true)
    public IngresosGastosDto ejecutar(Long empresaId, LocalDate desde, LocalDate hasta) {
        Long empresa = InformeDtoConverter.empresaObligatoria(empresaId);
        Periodo periodo = new Periodo(desde, hasta);
        List<FacturaResumida> facturas = InformeDtoConverter.aFacturasResumidas(
                facturaFacade.listarPorEmpresa(empresa, periodo.desde(), periodo.hasta()));
        return InformeDtoConverter.aDto(CalculadoraInformeFacturas.ingresosGastos(facturas));
    }
}
