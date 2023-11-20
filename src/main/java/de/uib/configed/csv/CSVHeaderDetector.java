/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CSVHeaderDetector {
	private static final Pattern containsDigitsPattern = Pattern.compile(".*\\d.*");

	private CSVFormatDetector csvFormatDetector;

	private boolean hasHeader;
	private String line;

	public CSVHeaderDetector(String line, CSVFormatDetector csvFormatDetector) {
		this.line = line;
		this.csvFormatDetector = csvFormatDetector;
	}

	private boolean containsDigits() {
		return containsDigitsPattern.matcher(line).matches();
	}

	private boolean containsEmptyFields() {
		String tmp = line.replace(String.valueOf(csvFormatDetector.getQuote()), "");
		return Arrays.stream(tmp.split(String.valueOf(csvFormatDetector.getDelimiter()))).anyMatch(String::isEmpty);
	}

	private boolean containsFieldsWithEmbeddedQuotes() {
		return line.contains(String.format("%c%c", csvFormatDetector.getQuote(), csvFormatDetector.getQuote()));
	}

	public String getHeader() {
		if (hasHeader) {
			return line;
		} else {
			return "";
		}
	}

	public boolean detect() {
		hasHeader = true;

		if (containsDigits() || containsEmptyFields() || containsFieldsWithEmbeddedQuotes()) {
			hasHeader = false;
			return hasHeader;
		}

		return hasHeader;
	}
}
