package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity.TipoCambioEntity;

import java.util.List;
import java.util.Optional;

public interface TipoCambioJpaRepository extends JpaRepository<TipoCambioEntity, Long> {
    Optional<TipoCambioEntity> findFirstByMonedaOrderByFechaRegistroDescIdDesc(String moneda);
    List<TipoCambioEntity> findByMonedaOrderByFechaRegistroDescIdDesc(String moneda);
}
