package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.result.CustomerView;

public interface SearchCustomersUseCase {

	PageResult<CustomerView> execute(String search, PageQuery page);

}
