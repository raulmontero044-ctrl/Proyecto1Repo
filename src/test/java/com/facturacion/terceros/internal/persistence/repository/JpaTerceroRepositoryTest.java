package com.facturacion.terceros.internal.persistence.repository;

import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.model.TipoTercero;
import com.facturacion.terceros.internal.persistence.dao.TerceroDao;
import com.facturacion.terceros.internal.persistence.entity.TerceroEntity;
import com.facturacion.terceros.internal.persistence.mapper.TerceroMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaTerceroRepositoryTest {

    private final TerceroDao dao = mock(TerceroDao.class);
    private final JpaTerceroRepository repository = new JpaTerceroRepository(dao, new TerceroMapper());

    private static TerceroEntity entidad(Long id, String nif) {
        return new TerceroEntity(
                id, 3L, nif, "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true, true);
    }

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(TerceroEntity.class))).thenReturn(entidad(5L, "B12345678"));
        Tercero nuevo = Tercero.nuevo(
                3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true);

        Tercero guardado = repository.guardar(nuevo);

        ArgumentCaptor<TerceroEntity> enviada = ArgumentCaptor.forClass(TerceroEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getEmpresaId()).isEqualTo(3L);
        assertThat(enviada.getValue().getNif()).isEqualTo("B12345678");
        assertThat(enviada.getValue().isActivo()).isTrue();
        assertThat(guardado.id()).isEqualTo(5L);
        assertThat(guardado.razonSocial()).isEqualTo("Tercero SL");
    }

    @Test
    void guardarUnTerceroExistenteEnviaSuIdentificador() {
        when(dao.save(any(TerceroEntity.class))).thenReturn(entidad(5L, "A87654321"));
        Tercero existente = new Tercero(
                5L, 3L, "A87654321", "Tercero SL", null, null, null, null, null, null, null, true, false, false);

        repository.guardar(existente);

        ArgumentCaptor<TerceroEntity> enviada = ArgumentCaptor.forClass(TerceroEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isEqualTo(5L);
        assertThat(enviada.getValue().getNif()).isEqualTo("A87654321");
        assertThat(enviada.getValue().isActivo()).isFalse();
    }

    @Test
    void buscarPorIdConvierteLaEntidadEncontrada() {
        when(dao.findById(5L)).thenReturn(Optional.of(entidad(5L, "B12345678")));

        assertThat(repository.buscarPorId(5L)).hasValueSatisfying(tercero -> {
            assertThat(tercero.id()).isEqualTo(5L);
            assertThat(tercero.nif()).isEqualTo("B12345678");
        });
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void listarPorEmpresaUsaLaConsultaDeLaEmpresaOrdenadaPorId() {
        when(dao.findAllByEmpresaIdOrderByIdAsc(3L))
                .thenReturn(List.of(entidad(1L, "B11111111"), entidad(2L, "B22222222")));

        List<Tercero> terceros = repository.listarPorEmpresa(3L);

        assertThat(terceros).extracting(Tercero::nif).containsExactly("B11111111", "B22222222");
        verify(dao, never()).findAll();
    }

    @Test
    void listarPorEmpresaYTipoClienteUsaLaConsultaDeClientes() {
        when(dao.findAllByEmpresaIdAndEsClienteTrueOrderByIdAsc(3L)).thenReturn(List.of(entidad(1L, "B11111111")));

        List<Tercero> terceros = repository.listarPorEmpresaYTipo(3L, TipoTercero.CLIENTE);

        assertThat(terceros).extracting(Tercero::nif).containsExactly("B11111111");
        verify(dao, never()).findAllByEmpresaIdAndEsProveedorTrueOrderByIdAsc(anyLong());
        verify(dao, never()).findAllByEmpresaIdOrderByIdAsc(anyLong());
    }

    @Test
    void listarPorEmpresaYTipoProveedorUsaLaConsultaDeProveedores() {
        when(dao.findAllByEmpresaIdAndEsProveedorTrueOrderByIdAsc(3L)).thenReturn(List.of(entidad(2L, "B22222222")));

        List<Tercero> terceros = repository.listarPorEmpresaYTipo(3L, TipoTercero.PROVEEDOR);

        assertThat(terceros).extracting(Tercero::nif).containsExactly("B22222222");
        verify(dao, never()).findAllByEmpresaIdAndEsClienteTrueOrderByIdAsc(anyLong());
        verify(dao, never()).findAllByEmpresaIdOrderByIdAsc(anyLong());
    }

    @Test
    void existePorEmpresaYNifUsaLaConsultaDeExistencia() {
        when(dao.existsByEmpresaIdAndNif(3L, "B12345678")).thenReturn(true);

        assertThat(repository.existePorEmpresaYNif(3L, "B12345678")).isTrue();
        assertThat(repository.existePorEmpresaYNif(4L, "B12345678")).isFalse();
        verify(dao, never()).findAll();
    }

    @Test
    void existePorEmpresaYNifEnOtroTerceroExcluyeElTerceroIndicado() {
        when(dao.existsByEmpresaIdAndNifAndIdNot(3L, "B12345678", 1L)).thenReturn(true);

        assertThat(repository.existePorEmpresaYNifEnOtroTercero(3L, "B12345678", 1L)).isTrue();
        assertThat(repository.existePorEmpresaYNifEnOtroTercero(3L, "B12345678", 2L)).isFalse();
        verify(dao, never()).existsByEmpresaIdAndNif(anyLong(), anyString());
        verify(dao, never()).findById(anyLong());
    }
}
