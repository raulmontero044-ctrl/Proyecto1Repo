package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarPagos {

    private final PagoRepository repository;

    public ListarPagos(PagoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PagoDto> ejecutar(Long facturaId, Long empresaId, String tipo) {
        return pagos(facturaId, empresaId, tipo).stream().map(PagoDtoConverter::aDto).toList();
    }

    private List<Pago> pagos(Long facturaId, Long empresaId, String tipo) {
        if (facturaId != null && empresaId == null && tipo == null) {
            return repository.listarPorFactura(facturaId);
        }
        if (facturaId == null && empresaId != null) {
            return tipo == null
                    ? repository.listarPorEmpresa(empresaId)
                    : repository.listarPorEmpresaYTipo(empresaId, PagoDtoConverter.aTipo(tipo));
        }
        throw new PagoInvalidoException("Indique facturaId, o bien empresaId con tipo opcional");
    }
}
