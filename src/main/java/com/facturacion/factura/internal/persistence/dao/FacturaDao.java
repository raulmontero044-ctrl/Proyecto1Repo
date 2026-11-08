package com.facturacion.factura.internal.persistence.dao;

import com.facturacion.factura.internal.persistence.entity.FacturaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FacturaDao extends JpaRepository<FacturaEntity, Long> {

    @EntityGraph(attributePaths = "lineas")
    Optional<FacturaEntity> findWithLineasById(Long id);

    @EntityGraph(attributePaths = "lineas")
    List<FacturaEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "lineas")
    List<FacturaEntity> findAllByEmpresaIdAndFechaBetweenOrderByFechaAscIdAsc(
            Long empresaId, LocalDate desde, LocalDate hasta);

    boolean existsByEmpresaIdAndTipoAndNumero(Long empresaId, String tipo, String numero);

    boolean existsByEmpresaIdAndTerceroIdAndTipoAndNumero(Long empresaId, Long terceroId, String tipo, String numero);
}
