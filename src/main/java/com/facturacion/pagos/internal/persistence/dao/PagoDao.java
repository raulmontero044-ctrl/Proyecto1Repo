package com.facturacion.pagos.internal.persistence.dao;

import com.facturacion.pagos.internal.persistence.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PagoDao extends JpaRepository<PagoEntity, Long> {

    List<PagoEntity> findAllByFacturaIdOrderByFechaAscIdAsc(Long facturaId);

    List<PagoEntity> findAllByEmpresaIdOrderByFechaAscIdAsc(Long empresaId);

    List<PagoEntity> findAllByEmpresaIdAndTipoOrderByFechaAscIdAsc(Long empresaId, String tipo);

    List<PagoEntity> findAllByEmpresaIdAndFechaBetweenOrderByFechaAscIdAsc(
            Long empresaId, LocalDate desde, LocalDate hasta);

    void deleteByFacturaId(Long facturaId);
}
