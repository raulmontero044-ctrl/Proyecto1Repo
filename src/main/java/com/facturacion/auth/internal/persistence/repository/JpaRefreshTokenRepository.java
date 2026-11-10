package com.facturacion.auth.internal.persistence.repository;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import com.facturacion.auth.internal.persistence.dao.RefreshTokenDao;
import com.facturacion.auth.internal.persistence.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final RefreshTokenDao dao;
    private final RefreshTokenMapper mapper;

    public JpaRefreshTokenRepository(RefreshTokenDao dao, RefreshTokenMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public RefreshToken guardar(RefreshToken refreshToken) {
        return mapper.aDominio(dao.save(mapper.aEntidad(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> buscarPorHash(String hash) {
        return dao.findByHash(hash).map(mapper::aDominio);
    }

    @Override
    public List<RefreshToken> buscarVigentesPorUsuario(Long usuarioId, Instant instante) {
        return dao.findByUsuarioIdAndRevocadoFalseAndExpiraEnAfter(usuarioId, instante).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public void eliminarRevocadosOCaducados(Instant instante) {
        dao.deleteRevocadosOCaducados(instante);
    }
}
