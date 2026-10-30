package com.mywallet.bank.application.service;

import com.mywallet.bank.domain.model.AdminAuditEntry;
import com.mywallet.bank.domain.port.AuditLogRepository;

import java.util.ArrayList;
import java.util.List;

final class InMemoryAuditLogRepository implements AuditLogRepository {

	private final List<AdminAuditEntry> entries = new ArrayList<>();

	@Override
	public AdminAuditEntry save(AdminAuditEntry entry) {
		entries.add(entry);
		return entry;
	}

	@Override
	public List<AdminAuditEntry> search(String actor, String action, int page, int size) {
		return List.copyOf(entries);
	}

	@Override
	public long count(String actor, String action) {
		return entries.size();
	}

}
