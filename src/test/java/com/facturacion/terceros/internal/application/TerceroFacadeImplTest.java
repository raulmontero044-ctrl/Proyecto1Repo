package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.TerceroFacade;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.model.Tercero;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TerceroFacadeImplTest {

    private final TerceroRepositoryEnMemoria repository = new TerceroRepositoryEnMemoria();
    private final TerceroFacade facade = new TerceroFacadeImpl(new BuscarTercero(repository));

    @Test
    void buscarDevuelveTodosLosDatosQueNecesitaElConsumidor() {
        Long id = repository.guardar(new Tercero(
                null, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", false, true, false)).id();

        assertThat(facade.buscar(id)).contains(new TerceroDto(
                id, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", false, true, false));
    }

    @Test
    void buscarUnTerceroInexistenteDevuelveVacioSinLanzarExcepcion() {
        assertThat(facade.buscar(99L)).isEmpty();
    }
}
