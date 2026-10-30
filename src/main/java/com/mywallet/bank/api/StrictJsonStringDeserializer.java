package com.mywallet.bank.api;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public final class StrictJsonStringDeserializer extends ValueDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext context) {
		if (!parser.hasToken(JsonToken.VALUE_STRING)) {
			throw context.wrongTokenException(
					parser,
					String.class,
					JsonToken.VALUE_STRING,
					"Un montant doit être une chaîne JSON"
			);
		}
		return parser.getText();
	}

}
