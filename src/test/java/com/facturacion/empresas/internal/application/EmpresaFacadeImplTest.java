package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.model.Empresa;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpresaFacadeImplTest {

    private final EmpresaRepositoryEnMemoria repository = new EmpresaRepositoryEnMemoria();
    private final EmpresaFacade facade = new EmpresaFacadeImpl(new BuscarEmpresa(repository));

    @Test
    void buscarDevuelveLaEmpresaConTodosSusCampos() {
        Long id = repository.guardar(new Empresa(
                null, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", false)).id();

        assertThat(facade.buscar(id)).contains(new EmpresaDto(
                id, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", false));
    }

    @Test
    void buscarEmpresaInexistenteDevuelveVacioSinLanzarExcepcion() {
        assertThat(facade.buscar(99L)).isEmpty();
    }

    @Test
    void buscarSoloDevuelveLaEmpresaIndicada() {
        repository.guardar(Empresa.nueva(
                "Primera SL", "B11111111", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null));
        Long segunda = repository.guardar(Empresa.nueva(
                "Segunda SL", "B22222222", "Calle Mayor 2", "28002", "Madrid", "Madrid", "España", null, null)).id();

        assertThat(facade.buscar(segunda)).map(EmpresaDto::nif).contains("B22222222");
    }
}
