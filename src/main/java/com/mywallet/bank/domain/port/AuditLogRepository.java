package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.AdminAuditEntry;

import java.util.List;

public interface AuditLogRepository {

	AdminAuditEntry save(AdminAuditEntry entry);

	List<AdminAuditEntry> search(String actor, String action, int page, int size);

	long count(String actor, String action);

}
