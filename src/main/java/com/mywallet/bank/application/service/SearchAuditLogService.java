package com.mywallet.bank.application.service;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.port.in.SearchAuditLogUseCase;
import com.mywallet.bank.application.result.AuditEntryView;
import com.mywallet.bank.domain.model.AdminAuditEntry;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.port.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchAuditLogService implements SearchAuditLogUseCase {

	private final AuditLogRepository auditLog;

	public SearchAuditLogService(AuditLogRepository auditLog) {
		this.auditLog = auditLog;
	}

	@Override
	public PageResult<AuditEntryView> execute(String actor, AuditAction action, PageQuery page) {
		String actorFilter = actor == null || actor.isBlank() ? "" : actor.trim();
		String actionFilter = action == null ? "" : action.name();
		return new PageResult<>(
				auditLog.search(actorFilter, actionFilter, page.page(), page.size()).stream()
						.map(SearchAuditLogService::view)
						.toList(),
				page.page(),
				page.size(),
				auditLog.count(actorFilter, actionFilter)
		);
	}

	private static AuditEntryView view(AdminAuditEntry entry) {
		return new AuditEntryView(
				entry.getActorId(),
				entry.getActorUsername(),
				entry.getAction(),
				entry.getTargetType(),
				entry.getTargetId(),
				entry.getDetails(),
				entry.getCreatedAt()
		);
	}

}
