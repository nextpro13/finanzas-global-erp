package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.TipoCambioRepositoryPort;
import pe.finanzasglobal.divisas.domain.model.TipoCambio;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity.TipoCambioEntity;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.repository.TipoCambioJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: traduce entre el modelo de dominio y JPA. */
@Component
public class TipoCambioPersistenceAdapter implements TipoCambioRepositoryPort {

    private final TipoCambioJpaRepository repo;

    public TipoCambioPersistenceAdapter(TipoCambioJpaRepository repo) { this.repo = repo; }

    @Override
    public TipoCambio guardar(TipoCambio tc) {
        return aDominio(repo.save(new TipoCambioEntity(tc.getId(), tc.getMoneda(), tc.getCompra(), tc.getVenta(),
                tc.getRegistradoPor(), tc.getFechaRegistro())));
    }

    @Override
    public Optional<TipoCambio> vigente(String moneda) {
        return repo.findFirstByMonedaOrderByFechaRegistroDescIdDesc(moneda).map(this::aDominio);
    }

    @Override
    public List<TipoCambio> historial(String moneda) {
        return repo.findByMonedaOrderByFechaRegistroDescIdDesc(moneda).stream().map(this::aDominio).toList();
    }

    private TipoCambio aDominio(TipoCambioEntity e) {
        return new TipoCambio(e.getId(), e.getMoneda(), e.getCompra(), e.getVenta(),
                e.getRegistradoPor(), e.getFechaRegistro());
    }
}
