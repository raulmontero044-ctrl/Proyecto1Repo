package com.facturacion.empresas.internal.persistence.dao;

import com.facturacion.empresas.internal.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaDao extends JpaRepository<EmpresaEntity, Long> {

    List<EmpresaEntity> findAllByOrderByIdAsc();

    boolean existsByNif(String nif);

    boolean existsByNifAndIdNot(String nif, Long id);
}
