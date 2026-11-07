package com.facturacion.empresas.internal.persistence.repository;

import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.persistence.dao.EmpresaDao;
import com.facturacion.empresas.internal.persistence.entity.EmpresaEntity;
import com.facturacion.empresas.internal.persistence.mapper.EmpresaMapper;
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

class JpaEmpresaRepositoryTest {

    private final EmpresaDao dao = mock(EmpresaDao.class);
    private final JpaEmpresaRepository repository = new JpaEmpresaRepository(dao, new EmpresaMapper());

    private static EmpresaEntity entidad(Long id, String nif) {
        return new EmpresaEntity(
                id, "Empresa SL", nif, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", true);
    }

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(EmpresaEntity.class))).thenReturn(entidad(5L, "B12345678"));
        Empresa nueva = Empresa.nueva(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000");

        Empresa guardada = repository.guardar(nueva);

        ArgumentCaptor<EmpresaEntity> enviada = ArgumentCaptor.forClass(EmpresaEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getNif()).isEqualTo("B12345678");
        assertThat(enviada.getValue().isActiva()).isTrue();
        assertThat(guardada.id()).isEqualTo(5L);
        assertThat(guardada.razonSocial()).isEqualTo("Empresa SL");
    }

    @Test
    void guardarUnaEmpresaExistenteEnviaSuIdentificador() {
        when(dao.save(any(EmpresaEntity.class))).thenReturn(entidad(5L, "A87654321"));
        Empresa existente = new Empresa(
                5L, "Empresa SL", "A87654321", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", true);

        repository.guardar(existente);

        ArgumentCaptor<EmpresaEntity> enviada = ArgumentCaptor.forClass(EmpresaEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isEqualTo(5L);
        assertThat(enviada.getValue().getNif()).isEqualTo("A87654321");
    }

    @Test
    void buscarPorIdConvierteLaEntidadEncontrada() {
        when(dao.findById(5L)).thenReturn(Optional.of(entidad(5L, "B12345678")));

        assertThat(repository.buscarPorId(5L)).hasValueSatisfying(empresa -> {
            assertThat(empresa.id()).isEqualTo(5L);
            assertThat(empresa.nif()).isEqualTo("B12345678");
        });
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void listarUsaLaConsultaOrdenadaPorId() {
        when(dao.findAllByOrderByIdAsc()).thenReturn(List.of(entidad(1L, "B11111111"), entidad(2L, "B22222222")));

        List<Empresa> empresas = repository.listar();

        assertThat(empresas).extracting(Empresa::nif).containsExactly("B11111111", "B22222222");
        verify(dao, never()).findAll();
    }

    @Test
    void existePorNifUsaLaConsultaDeExistencia() {
        when(dao.existsByNif("B12345678")).thenReturn(true);

        assertThat(repository.existePorNif("B12345678")).isTrue();
        assertThat(repository.existePorNif("A87654321")).isFalse();
        verify(dao, never()).findAll();
    }

    @Test
    void existePorNifEnOtraEmpresaExcluyeLaEmpresaIndicada() {
        when(dao.existsByNifAndIdNot("B12345678", 1L)).thenReturn(true);

        assertThat(repository.existePorNifEnOtraEmpresa("B12345678", 1L)).isTrue();
        assertThat(repository.existePorNifEnOtraEmpresa("B12345678", 2L)).isFalse();
        verify(dao, never()).existsByNif(anyString());
        verify(dao, never()).findById(anyLong());
    }
}
