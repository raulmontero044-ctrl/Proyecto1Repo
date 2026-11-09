package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.informes.internal.domain.model.Agrupacion;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.Periodo;
import com.facturacion.informes.internal.domain.service.CalculadoraInformeFacturas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsultarEstadisticas {

    private final FacturaFacade facturaFacade;

    public ConsultarEstadisticas(FacturaFacade facturaFacade) {
        this.facturaFacade = facturaFacade;
    }

    @Transactional(readOnly = true)
    public List<EstadisticaPeriodoDto> ejecutar(
            Long empresaId, LocalDate desde, LocalDate hasta, String agrupacion) {
        Long empresa = InformeDtoConverter.empresaObligatoria(empresaId);
        Periodo periodo = new Periodo(desde, hasta);
        Agrupacion agrupacionPedida = Agrupacion.desde(agrupacion);
        List<FacturaResumida> facturas = InformeDtoConverter.aFacturasResumidas(
                facturaFacade.listarPorEmpresa(empresa, periodo.desde(), periodo.hasta()));
        return InformeDtoConverter.aEstadisticasDto(
                CalculadoraInformeFacturas.estadisticas(facturas, agrupacionPedida));
    }
}
