package com.mywallet.bank.application.service;

final class PhoneNumbers {

	private PhoneNumbers() {
	}

	static String normalize(String phone) {
		if (phone == null) {
			return "";
		}
		return phone.replaceAll("[\\s-]", "");
	}

}
