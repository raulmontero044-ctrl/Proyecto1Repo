package com.facturacion.terceros.internal.persistence.dao;

import com.facturacion.terceros.internal.persistence.entity.TerceroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerceroDao extends JpaRepository<TerceroEntity, Long> {

    List<TerceroEntity> findAllByEmpresaIdOrderByIdAsc(Long empresaId);

    List<TerceroEntity> findAllByEmpresaIdAndEsClienteTrueOrderByIdAsc(Long empresaId);

    List<TerceroEntity> findAllByEmpresaIdAndEsProveedorTrueOrderByIdAsc(Long empresaId);

    boolean existsByEmpresaIdAndNif(Long empresaId, String nif);

    boolean existsByEmpresaIdAndNifAndIdNot(Long empresaId, String nif, Long id);
}
