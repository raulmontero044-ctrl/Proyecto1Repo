package com.facturacion.terceros.internal.persistence.mapper;

import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.persistence.entity.TerceroEntity;
import org.springframework.stereotype.Component;

@Component
public class TerceroMapper {

    public TerceroEntity aEntidad(Tercero tercero) {
        return new TerceroEntity(
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

    public Tercero aDominio(TerceroEntity entidad) {
        return new Tercero(
                entidad.getId(),
                entidad.getEmpresaId(),
                entidad.getNif(),
                entidad.getRazonSocial(),
                entidad.getDireccion(),
                entidad.getCodigoPostal(),
                entidad.getMunicipio(),
                entidad.getProvincia(),
                entidad.getPais(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.isEsCliente(),
                entidad.isEsProveedor(),
                entidad.isActivo());
    }
}
