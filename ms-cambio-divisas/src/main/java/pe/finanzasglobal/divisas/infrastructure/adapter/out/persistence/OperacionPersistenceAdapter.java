package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.OperacionRepositoryPort;
import pe.finanzasglobal.divisas.domain.model.OperacionCambio;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity.OperacionCambioEntity;
import pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.repository.OperacionCambioJpaRepository;

import java.util.Optional;

@Component
public class OperacionPersistenceAdapter implements OperacionRepositoryPort {

    private final OperacionCambioJpaRepository repo;

    public OperacionPersistenceAdapter(OperacionCambioJpaRepository repo) { this.repo = repo; }

    @Override
    public OperacionCambio guardar(OperacionCambio o) {
        return aDominio(repo.saveAndFlush(new OperacionCambioEntity(o.getId(), o.getIdempotencyKey(),
                o.getClienteId(), o.getTipo(), o.getMoneda(), o.getMontoDivisa(), o.getTasaAplicada(),
                o.getTipoCambioId(), o.getMontoSoles(), o.getComision(), o.getTotalSoles(), o.getEstado(),
                o.getUsuario(), o.getOficina(), o.getFechaRegistro(), o.getAprobadoPor(),
                o.getFechaAprobacion(), o.getVersion())));
    }

    @Override
    public Optional<OperacionCambio> buscarPorId(Long id) { return repo.findById(id).map(this::aDominio); }

    @Override
    public Optional<OperacionCambio> buscarPorIdempotencyKey(String key) {
        return repo.findByIdempotencyKey(key).map(this::aDominio);
    }

    private OperacionCambio aDominio(OperacionCambioEntity e) {
        return new OperacionCambio(e.getId(), e.getIdempotencyKey(), e.getClienteId(), e.getTipo(), e.getMoneda(),
                e.getMontoDivisa(), e.getTasaAplicada(), e.getTipoCambioId(), e.getMontoSoles(), e.getComision(),
                e.getTotalSoles(), e.getEstado(), e.getUsuario(), e.getOficina(), e.getFechaRegistro(),
                e.getAprobadoPor(), e.getFechaAprobacion(), e.getVersion());
    }
}
