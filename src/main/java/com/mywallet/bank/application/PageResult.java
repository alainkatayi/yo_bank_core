package com.mywallet.bank.application;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

	public PageResult {
		items = List.copyOf(items);
		if (page < 0 || size < 1 || totalItems < 0) {
			throw new IllegalArgumentException("La page est invalide");
		}
	}

}
