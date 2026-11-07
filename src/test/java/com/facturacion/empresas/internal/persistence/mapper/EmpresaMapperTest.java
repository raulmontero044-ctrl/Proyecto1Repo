package com.facturacion.empresas.internal.persistence.mapper;

import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;
import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.persistence.entity.EmpresaEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmpresaMapperTest {

    private final EmpresaMapper mapper = new EmpresaMapper();

    @Test
    void conservaTodosLosDatosEnIdaYVuelta() {
        Empresa empresa = new Empresa(
                7L, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", false);

        assertThat(mapper.aDominio(mapper.aEntidad(empresa))).isEqualTo(empresa);
    }

    @Test
    void empresaNuevaGeneraEntidadSinIdYConDatosCopiados() {
        Empresa nueva = Empresa.nueva(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000");

        EmpresaEntity entidad = mapper.aEntidad(nueva);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getRazonSocial()).isEqualTo("Empresa SL");
        assertThat(entidad.getNif()).isEqualTo("B12345678");
        assertThat(entidad.getDireccion()).isEqualTo("Calle Mayor 1");
        assertThat(entidad.getCodigoPostal()).isEqualTo("28001");
        assertThat(entidad.getMunicipio()).isEqualTo("Madrid");
        assertThat(entidad.getProvincia()).isEqualTo("Madrid");
        assertThat(entidad.getPais()).isEqualTo("España");
        assertThat(entidad.getEmail()).isEqualTo("info@empresa.es");
        assertThat(entidad.getTelefono()).isEqualTo("910000000");
        assertThat(entidad.isActiva()).isTrue();
    }

    @Test
    void aDominioConservaElContactoAusente() {
        EmpresaEntity entidad = new EmpresaEntity(
                3L, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                null, null, true);

        Empresa empresa = mapper.aDominio(entidad);

        assertThat(empresa.id()).isEqualTo(3L);
        assertThat(empresa.email()).isNull();
        assertThat(empresa.telefono()).isNull();
        assertThat(empresa.activa()).isTrue();
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        EmpresaEntity sinNif = new EmpresaEntity(
                3L, "Empresa SL", " ", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                null, null, true);

        assertThatThrownBy(() -> mapper.aDominio(sinNif)).isInstanceOf(EmpresaInvalidaException.class);
    }
}
