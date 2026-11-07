package com.facturacion.usuarios.internal.persistence.dao;

import com.facturacion.usuarios.internal.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = "accesos")
    Optional<UsuarioEntity> findWithAccesosById(Long id);

    @EntityGraph(attributePaths = "accesos")
    Optional<UsuarioEntity> findWithAccesosByEmail(String email);

    @EntityGraph(attributePaths = "accesos")
    List<UsuarioEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "accesos")
    @Query("""
            select u from UsuarioEntity u
            where exists (
                select c.id from UsuarioEntity c join c.accesos a
                where c.id = u.id and a.empresaId = :empresaId)
            order by u.id asc
            """)
    List<UsuarioEntity> findAllConAccesoAEmpresa(@Param("empresaId") Long empresaId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
