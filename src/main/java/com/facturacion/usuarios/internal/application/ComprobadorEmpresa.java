package com.facturacion.usuarios.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.usuarios.internal.domain.exception.EmpresaInexistenteException;
import org.springframework.stereotype.Component;

@Component
class ComprobadorEmpresa {

    private final EmpresaFacade empresaFacade;

    ComprobadorEmpresa(EmpresaFacade empresaFacade) {
        this.empresaFacade = empresaFacade;
    }

    void exigirExistente(Long empresaId) {
        if (empresaFacade.buscar(empresaId).isEmpty()) {
            throw new EmpresaInexistenteException(empresaId);
        }
    }
}
