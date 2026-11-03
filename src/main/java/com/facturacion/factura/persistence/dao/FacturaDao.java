package com.facturacion.factura.persistence.dao;

import com.facturacion.factura.persistence.entity.FacturaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaDao extends JpaRepository<FacturaEntity, Long> {

    @EntityGraph(attributePaths = "lineas")
    Optional<FacturaEntity> findWithLineasById(Long id);

    @EntityGraph(attributePaths = "lineas")
    List<FacturaEntity> findAllByOrderByIdAsc();

    boolean existsByNumero(String numero);
}
