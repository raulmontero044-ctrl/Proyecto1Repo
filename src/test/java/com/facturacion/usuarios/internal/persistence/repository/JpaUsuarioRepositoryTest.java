package com.facturacion.usuarios.internal.persistence.repository;

import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.persistence.dao.UsuarioDao;
import com.facturacion.usuarios.internal.persistence.entity.AccesoEmpresaEmbeddable;
import com.facturacion.usuarios.internal.persistence.entity.UsuarioEntity;
import com.facturacion.usuarios.internal.persistence.mapper.UsuarioMapper;
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

class JpaUsuarioRepositoryTest {

    private final UsuarioDao dao = mock(UsuarioDao.class);
    private final JpaUsuarioRepository repository = new JpaUsuarioRepository(dao, new UsuarioMapper());

    private static UsuarioEntity entidad(Long id, String email) {
        return new UsuarioEntity(id, email, "Ana", "hash", true, false, List.of(new AccesoEmpresaEmbeddable(3L, "GESTOR")));
    }

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(UsuarioEntity.class))).thenReturn(entidad(5L, "ana@empresa.es"));
        Usuario nuevo = Usuario.nuevo("ana@empresa.es", "Ana", "hash", List.of(new AccesoEmpresa(3L, Rol.GESTOR)));

        Usuario guardado = repository.guardar(nuevo);

        ArgumentCaptor<UsuarioEntity> enviada = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getEmail()).isEqualTo("ana@empresa.es");
        assertThat(enviada.getValue().getAccesos()).extracting(AccesoEmpresaEmbeddable::getEmpresaId).containsExactly(3L);
        assertThat(guardado.id()).isEqualTo(5L);
        assertThat(guardado.accesos()).containsExactly(new AccesoEmpresa(3L, Rol.GESTOR));
    }

    @Test
    void guardarUnUsuarioExistenteEnviaSuIdentificador() {
        when(dao.save(any(UsuarioEntity.class))).thenReturn(entidad(5L, "otro@empresa.es"));

        repository.guardar(new Usuario(5L, "otro@empresa.es", "Ana", "hash", false, false, List.of()));

        ArgumentCaptor<UsuarioEntity> enviada = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isEqualTo(5L);
        assertThat(enviada.getValue().isActivo()).isFalse();
        assertThat(enviada.getValue().getAccesos()).isEmpty();
    }

    @Test
    void buscarPorIdCargaLosAccesosEnLaMismaConsulta() {
        when(dao.findWithAccesosById(5L)).thenReturn(Optional.of(entidad(5L, "ana@empresa.es")));

        assertThat(repository.buscarPorId(5L)).hasValueSatisfying(usuario -> {
            assertThat(usuario.id()).isEqualTo(5L);
            assertThat(usuario.accesos()).containsExactly(new AccesoEmpresa(3L, Rol.GESTOR));
        });
        verify(dao, never()).findById(anyLong());
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findWithAccesosById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void buscarPorEmailUsaLaConsultaPorEmail() {
        when(dao.findWithAccesosByEmail("ana@empresa.es")).thenReturn(Optional.of(entidad(5L, "ana@empresa.es")));

        assertThat(repository.buscarPorEmail("ana@empresa.es")).map(Usuario::id).contains(5L);
        assertThat(repository.buscarPorEmail("otro@empresa.es")).isEmpty();
    }

    @Test
    void listarUsaLaConsultaOrdenadaPorId() {
        when(dao.findAllByOrderByIdAsc()).thenReturn(List.of(entidad(1L, "a@empresa.es"), entidad(2L, "b@empresa.es")));

        List<Usuario> usuarios = repository.listar();

        assertThat(usuarios).extracting(Usuario::email).containsExactly("a@empresa.es", "b@empresa.es");
        verify(dao, never()).findAll();
    }

    @Test
    void listarPorEmpresaUsaLaConsultaFiltrada() {
        when(dao.findAllConAccesoAEmpresa(3L)).thenReturn(List.of(entidad(2L, "b@empresa.es")));

        assertThat(repository.listarPorEmpresa(3L)).extracting(Usuario::id).containsExactly(2L);
        verify(dao, never()).findAllByOrderByIdAsc();
    }

    @Test
    void existePorEmailUsaLaConsultaDeExistencia() {
        when(dao.existsByEmail("ana@empresa.es")).thenReturn(true);

        assertThat(repository.existePorEmail("ana@empresa.es")).isTrue();
        assertThat(repository.existePorEmail("otro@empresa.es")).isFalse();
        verify(dao, never()).findWithAccesosByEmail(anyString());
    }

    @Test
    void existePorEmailEnOtroUsuarioExcluyeElUsuarioIndicado() {
        when(dao.existsByEmailAndIdNot("ana@empresa.es", 1L)).thenReturn(true);

        assertThat(repository.existePorEmailEnOtroUsuario("ana@empresa.es", 1L)).isTrue();
        assertThat(repository.existePorEmailEnOtroUsuario("ana@empresa.es", 2L)).isFalse();
        verify(dao, never()).existsByEmail(anyString());
    }

    @Test
    void existeAlgunoConsultaElRecuentoDelDao() {
        when(dao.count()).thenReturn(0L);
        assertThat(repository.existeAlguno()).isFalse();

        when(dao.count()).thenReturn(1L);
        assertThat(repository.existeAlguno()).isTrue();
    }
}
