package com.mywallet.bank.application;

import com.mywallet.bank.application.exception.InvalidQueryException;

public record PageQuery(int page, int size) {

	public static final int MAX_SIZE = 100;

	public PageQuery {
		if (page < 0 || size < 1) {
			throw new InvalidQueryException();
		}
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}
	}

}
