/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

public class CSVToken {
	public static final String EMBEDDED_QUOTE = "EMBEDDED_QUOTE";
	public static final String STRING_SEPARATOR = "STRING_SEPARATOR";
	public static final String FIELD_SEPARATOR = "FIELD_SEPARATOR";
	public static final String EMPTY_FIELD = "EMPTY_FIELD";
	public static final String FIELD = "FIELD";
	public static final String NEW_LINE = "NEW_LINE";
	public static final String LINE_END = "LINE_END";

	private String token;
	private String value;

	public CSVToken(String token, String value) {
		this.token = token;
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String getName() {
		return token;
	}

	public boolean tokenEquals(String token) {
		return token.equals(this.token);
	}

	@Override
	public String toString() {
		return String.format("%s: %s", token, value);
	}
}
