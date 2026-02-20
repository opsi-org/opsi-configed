/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.csv;

import java.util.Arrays;
import java.util.regex.Pattern;

import de.uib.configed.share.logging.Logging;

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
		return hasHeader ? line : "";
	}

	public boolean detect() {
		hasHeader = true;

		boolean containsDigits = containsDigits();
		boolean containsEmptyFields = containsEmptyFields();
		boolean containsFieldsWithEmbeddedQuotes = containsFieldsWithEmbeddedQuotes();
		boolean containsNotAllowedCharactersInHeader = containsDigits || containsEmptyFields
				|| containsFieldsWithEmbeddedQuotes;

		Logging.debug(this, "Checking if the line is a header:", line);
		Logging.debug(this, "Line check details: contains digits =", containsDigits, ", contains empty fields =",
				containsEmptyFields, ", contains fields with embedded quotes =", containsFieldsWithEmbeddedQuotes);
		Logging.debug(this, "Does the line contain any not allowed characters?", containsNotAllowedCharactersInHeader);

		if (containsNotAllowedCharactersInHeader) {
			hasHeader = false;

			return hasHeader;
		}

		return hasHeader;
	}
}
