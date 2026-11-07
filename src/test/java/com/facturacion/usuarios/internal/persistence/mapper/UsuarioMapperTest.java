package com.facturacion.usuarios.internal.persistence.mapper;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.persistence.entity.AccesoEmpresaEmbeddable;
import com.facturacion.usuarios.internal.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void idaYVueltaConservaTodosLosCamposYElOrdenDeLosAccesos() {
        Usuario original = new Usuario(
                4L, "ana@empresa.es", "Ana", "hash", false,
                List.of(new AccesoEmpresa(2L, Rol.ADMINISTRADOR), new AccesoEmpresa(1L, Rol.CONSULTA)));

        Usuario reconstruido = mapper.aDominio(mapper.aEntidad(original));

        assertThat(reconstruido).isEqualTo(original);
    }

    @Test
    void aEntidadTrasladaElRolComoTexto() {
        Usuario usuario = Usuario.nuevo("ana@empresa.es", "Ana", "hash", List.of(new AccesoEmpresa(3L, Rol.GESTOR)));

        UsuarioEntity entidad = mapper.aEntidad(usuario);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getEmail()).isEqualTo("ana@empresa.es");
        assertThat(entidad.getNombre()).isEqualTo("Ana");
        assertThat(entidad.getPasswordHash()).isEqualTo("hash");
        assertThat(entidad.isActivo()).isTrue();
        assertThat(entidad.getAccesos()).singleElement().satisfies(acceso -> {
            assertThat(acceso.getEmpresaId()).isEqualTo(3L);
            assertThat(acceso.getRol()).isEqualTo("GESTOR");
        });
    }

    @Test
    void aDominioSinAccesosDevuelveUnaListaVacia() {
        UsuarioEntity entidad = new UsuarioEntity(1L, "ana@empresa.es", "Ana", "hash", true, List.of());

        assertThat(mapper.aDominio(entidad).accesos()).isEmpty();
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        UsuarioEntity entidad = new UsuarioEntity(1L, "ana@empresa.es", " ", "hash", true, List.of());

        assertThatThrownBy(() -> mapper.aDominio(entidad)).isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void aDominioConvierteCadaRol() {
        UsuarioEntity entidad = new UsuarioEntity(1L, "ana@empresa.es", "Ana", "hash", true, List.of(
                new AccesoEmpresaEmbeddable(1L, "ADMINISTRADOR"),
                new AccesoEmpresaEmbeddable(2L, "GESTOR"),
                new AccesoEmpresaEmbeddable(3L, "CONSULTA")));

        assertThat(mapper.aDominio(entidad).accesos()).containsExactly(
                new AccesoEmpresa(1L, Rol.ADMINISTRADOR),
                new AccesoEmpresa(2L, Rol.GESTOR),
                new AccesoEmpresa(3L, Rol.CONSULTA));
    }
}
