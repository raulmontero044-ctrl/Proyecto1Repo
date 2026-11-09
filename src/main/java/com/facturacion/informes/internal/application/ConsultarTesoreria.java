package com.facturacion.informes.internal.application;

import com.facturacion.informes.internal.domain.model.PagoResumido;
import com.facturacion.informes.internal.domain.model.Periodo;
import com.facturacion.informes.internal.domain.service.CalculadoraInformePagos;
import com.facturacion.pagos.api.facade.PagoFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsultarTesoreria {

    private final PagoFacade pagoFacade;

    public ConsultarTesoreria(PagoFacade pagoFacade) {
        this.pagoFacade = pagoFacade;
    }

    @Transactional(readOnly = true)
    public ResumenTesoreriaDto ejecutar(Long empresaId, LocalDate desde, LocalDate hasta) {
        Long empresa = InformeDtoConverter.empresaObligatoria(empresaId);
        Periodo periodo = new Periodo(desde, hasta);
        List<PagoResumido> pagos = InformeDtoConverter.aPagosResumidos(
                pagoFacade.listarPorEmpresa(empresa, periodo.desde(), periodo.hasta()));
        return InformeDtoConverter.aDto(CalculadoraInformePagos.tesoreria(pagos));
    }
}
