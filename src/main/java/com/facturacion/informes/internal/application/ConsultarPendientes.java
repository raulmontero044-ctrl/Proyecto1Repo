package com.facturacion.informes.internal.application;

import com.facturacion.informes.internal.domain.model.SaldoFactura;
import com.facturacion.informes.internal.domain.service.CalculadoraInformePagos;
import com.facturacion.pagos.api.facade.PagoFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultarPendientes {

    private final PagoFacade pagoFacade;

    public ConsultarPendientes(PagoFacade pagoFacade) {
        this.pagoFacade = pagoFacade;
    }

    @Transactional(readOnly = true)
    public ResumenPendientesDto ejecutar(Long empresaId) {
        Long empresa = InformeDtoConverter.empresaObligatoria(empresaId);
        List<SaldoFactura> saldos = InformeDtoConverter.aSaldosFactura(pagoFacade.estadosPorEmpresa(empresa));
        return InformeDtoConverter.aDto(CalculadoraInformePagos.pendientes(saldos));
    }
}
