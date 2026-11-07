package com.facturacion.terceros.internal.persistence.mapper;

import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.persistence.entity.TerceroEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerceroMapperTest {

    private final TerceroMapper mapper = new TerceroMapper();

    @Test
    void conservaTodosLosDatosEnIdaYVuelta() {
        Tercero tercero = new Tercero(
                7L, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false, false);

        assertThat(mapper.aDominio(mapper.aEntidad(tercero))).isEqualTo(tercero);
    }

    @Test
    void terceroNuevoGeneraEntidadSinIdYConDatosCopiados() {
        Tercero nuevo = Tercero.nuevo(
                3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", false, true);

        TerceroEntity entidad = mapper.aEntidad(nuevo);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getEmpresaId()).isEqualTo(3L);
        assertThat(entidad.getNif()).isEqualTo("B12345678");
        assertThat(entidad.getRazonSocial()).isEqualTo("Tercero SL");
        assertThat(entidad.getDireccion()).isEqualTo("Calle Mayor 1");
        assertThat(entidad.getCodigoPostal()).isEqualTo("28001");
        assertThat(entidad.getMunicipio()).isEqualTo("Madrid");
        assertThat(entidad.getProvincia()).isEqualTo("Madrid");
        assertThat(entidad.getPais()).isEqualTo("España");
        assertThat(entidad.getEmail()).isEqualTo("info@tercero.es");
        assertThat(entidad.getTelefono()).isEqualTo("910000000");
        assertThat(entidad.isEsCliente()).isFalse();
        assertThat(entidad.isEsProveedor()).isTrue();
        assertThat(entidad.isActivo()).isTrue();
    }

    @Test
    void aDominioConservaLosDatosOpcionalesAusentes() {
        TerceroEntity entidad = new TerceroEntity(
                3L, 5L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, true, true, true);

        Tercero tercero = mapper.aDominio(entidad);

        assertThat(tercero).isEqualTo(new Tercero(
                3L, 5L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, true, true, true));
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        TerceroEntity niClienteNiProveedor = new TerceroEntity(
                3L, 5L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, false, false, true);

        assertThatThrownBy(() -> mapper.aDominio(niClienteNiProveedor)).isInstanceOf(TerceroInvalidoException.class);
    }
}
