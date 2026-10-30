package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.result.AuditEntryView;
import com.mywallet.bank.domain.model.AuditAction;

public interface SearchAuditLogUseCase {

	PageResult<AuditEntryView> execute(String actor, AuditAction action, PageQuery page);

}
