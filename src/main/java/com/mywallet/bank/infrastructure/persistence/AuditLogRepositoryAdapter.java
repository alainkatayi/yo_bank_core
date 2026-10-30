package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AdminAuditEntry;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.port.AuditLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AuditLogRepositoryAdapter implements AuditLogRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public AdminAuditEntry save(AdminAuditEntry entry) {
		entityManager.persist(toEntity(entry));
		entityManager.flush();
		return entry;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminAuditEntry> search(String actor, String action, int page, int size) {
		boolean filterAction = action != null && !action.isBlank();
		return entityManager.createQuery("""
						select e from AdminAuditLogJpaEntity e
						where (:actor = '' or e.actorId = :actor or e.actorUsername = :actor)
						  and (:filterAction = false or e.action = :action)
						order by e.createdAt desc
						""", AdminAuditLogJpaEntity.class)
				.setParameter("actor", actor == null ? "" : actor)
				.setParameter("filterAction", filterAction)
				.setParameter("action", filterAction ? AuditAction.valueOf(action) : AuditAction.CUSTOMER_CREATED)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList()
				.stream()
				.map(AuditLogRepositoryAdapter::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public long count(String actor, String action) {
		boolean filterAction = action != null && !action.isBlank();
		return entityManager.createQuery("""
						select count(e) from AdminAuditLogJpaEntity e
						where (:actor = '' or e.actorId = :actor or e.actorUsername = :actor)
						  and (:filterAction = false or e.action = :action)
						""", Long.class)
				.setParameter("actor", actor == null ? "" : actor)
				.setParameter("filterAction", filterAction)
				.setParameter("action", filterAction ? AuditAction.valueOf(action) : AuditAction.CUSTOMER_CREATED)
				.getSingleResult();
	}

	private static AdminAuditLogJpaEntity toEntity(AdminAuditEntry entry) {
		return new AdminAuditLogJpaEntity(
				entry.getId(),
				entry.getActorId(),
				entry.getActorUsername(),
				entry.getAction(),
				entry.getTargetType(),
				entry.getTargetId(),
				entry.getDetails(),
				entry.getCreatedAt()
		);
	}

	private static AdminAuditEntry toDomain(AdminAuditLogJpaEntity entity) {
		return new AdminAuditEntry(
				entity.getId(),
				entity.getActorId(),
				entity.getActorUsername(),
				entity.getAction(),
				entity.getTargetType(),
				entity.getTargetId(),
				entity.getDetails(),
				entity.getCreatedAt()
		);
	}

}
