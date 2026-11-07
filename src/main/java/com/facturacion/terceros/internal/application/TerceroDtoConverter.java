package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.dto.NuevoTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.model.TipoTercero;

final class TerceroDtoConverter {

    private TerceroDtoConverter() {
    }

    static Tercero aDominio(NuevoTerceroDto dto) {
        return Tercero.nuevo(
                dto.empresaId(),
                dto.nif(),
                dto.razonSocial(),
                dto.direccion(),
                dto.codigoPostal(),
                dto.municipio(),
                dto.provincia(),
                dto.pais(),
                dto.email(),
                dto.telefono(),
                dto.esCliente(),
                dto.esProveedor());
    }

    static TerceroDto aDto(Tercero tercero) {
        return new TerceroDto(
                tercero.id(),
                tercero.empresaId(),
                tercero.nif(),
                tercero.razonSocial(),
                tercero.direccion(),
                tercero.codigoPostal(),
                tercero.municipio(),
                tercero.provincia(),
                tercero.pais(),
                tercero.email(),
                tercero.telefono(),
                tercero.esCliente(),
                tercero.esProveedor(),
                tercero.activo());
    }

    static TipoTercero aTipo(String tipo) {
        try {
            return TipoTercero.valueOf(tipo);
        } catch (IllegalArgumentException excepcion) {
            throw new TerceroInvalidoException("El tipo de tercero no es válido");
        }
    }
}
