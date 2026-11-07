package com.facturacion.empresas.internal.persistence.mapper;

import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.persistence.entity.EmpresaEntity;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {

    public EmpresaEntity aEntidad(Empresa empresa) {
        return new EmpresaEntity(
                empresa.id(),
                empresa.razonSocial(),
                empresa.nif(),
                empresa.direccion(),
                empresa.codigoPostal(),
                empresa.municipio(),
                empresa.provincia(),
                empresa.pais(),
                empresa.email(),
                empresa.telefono(),
                empresa.activa());
    }

    public Empresa aDominio(EmpresaEntity entidad) {
        return new Empresa(
                entidad.getId(),
                entidad.getRazonSocial(),
                entidad.getNif(),
                entidad.getDireccion(),
                entidad.getCodigoPostal(),
                entidad.getMunicipio(),
                entidad.getProvincia(),
                entidad.getPais(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.isActiva());
    }
}
