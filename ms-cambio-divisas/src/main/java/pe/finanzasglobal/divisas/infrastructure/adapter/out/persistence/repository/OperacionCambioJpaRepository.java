package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity.OperacionCambioEntity;

import java.util.Optional;

public interface OperacionCambioJpaRepository extends JpaRepository<OperacionCambioEntity, Long> {
    Optional<OperacionCambioEntity> findByIdempotencyKey(String idempotencyKey);
}
