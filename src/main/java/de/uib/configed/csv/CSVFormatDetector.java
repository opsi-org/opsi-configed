/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class CSVFormatDetector {
	private static final Pattern containsDigitsPattern = Pattern.compile(".*\\d.*");
	private static final Pattern doubleMinusPattern = Pattern.compile("--");

	private static final char DEFAULT_FIELD_SEPARATOR = ',';
	private static final char DEFAULT_STRING_SEPARATOR = '"';
	private static final String FORMAT_HINT_INDICATOR = "#";

	private char fieldSeparator = DEFAULT_FIELD_SEPARATOR;
	private char stringSeparator = DEFAULT_STRING_SEPARATOR;
	private boolean hasHeader;
	private boolean hasHint;

	private Map<Character, Integer> possibleFieldSeparators = new HashMap<>();
	private Map<Character, Long> possibleStringSeparators = new HashMap<>();

	private List<String> headers;

	public char getFieldSeparator() {
		return this.fieldSeparator;
	}

	public void setFieldSeparator(char fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	public char getStringSeparator() {
		return stringSeparator;
	}

	public void setStringSeparator(char stringSeparator) {
		this.stringSeparator = stringSeparator;
	}

	public boolean hasHeader() {
		return hasHeader;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public boolean hasHint() {
		return hasHint;
	}

	public void detectFormat(String csvFile) throws IOException {
		Path path = Paths.get(csvFile);
		List<String> fileAsList = Files.readAllLines(path, StandardCharsets.UTF_8);
		String fileAsString = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

		if (fileAsList.get(0).startsWith(FORMAT_HINT_INDICATOR)) {
			hasHint = true;

			String[] formatOptions = doubleMinusPattern
					.split(fileAsList.get(0).replace(FORMAT_HINT_INDICATOR, "").trim());

			fieldSeparator = formatOptions[0].charAt(formatOptions[0].indexOf("=") + 1);
			stringSeparator = formatOptions[1].charAt(formatOptions[1].indexOf("=") + 1);
		} else {
			hasHint = false;

			detectStringSeparator(fileAsString);
			detectFieldSeparator(fileAsList);
		}

		detectHeader(fileAsList);
	}

	private void detectFieldSeparator(List<String> fileAsList) {
		boolean inQuotes = false;

		for (int i = 0; i < fileAsList.size(); i++) {
			String line = fileAsList.get(i);

			for (char c : line.toCharArray()) {
				if (c == stringSeparator) {
					inQuotes = !inQuotes;
					continue;
				}

				if (!Character.isLetter(c) && !Character.isDigit(c) && !inQuotes) {
					if (!possibleFieldSeparators.containsKey(c)) {
						possibleFieldSeparators.put(c, 1);
					} else {
						possibleFieldSeparators.put(c, possibleFieldSeparators.get(c) + 1);
					}
				}
			}
		}

		fieldSeparator = Collections.max(possibleFieldSeparators.entrySet(), Entry.comparingByValue()).getKey();
	}

	private void detectStringSeparator(String fileAsString) {
		char singleQuote = '\'';
		char doubleQuote = '"';

		long singleQuoteCount = fileAsString.chars().filter(ch -> ch == singleQuote).count();
		long doubleQuoteCount = fileAsString.chars().filter(ch -> ch == doubleQuote).count();

		possibleStringSeparators.put(singleQuote, singleQuoteCount);
		possibleStringSeparators.put(doubleQuote, doubleQuoteCount);

		stringSeparator = Collections.max(possibleStringSeparators.entrySet(), Entry.comparingByValue()).getKey();
	}

	private void detectHeader(List<String> fileAsList) {
		int lineNumber = 0;
		if (fileAsList.get(0).startsWith(FORMAT_HINT_INDICATOR)) {
			lineNumber = 1;
		}

		HeaderDetector detector = new HeaderDetector(fileAsList.get(lineNumber));
		hasHeader = detector.detect();
		String header = detector.getHeader();
		headers = Arrays
				.asList(header.replace(String.valueOf(stringSeparator), "").split(String.valueOf(getFieldSeparator())));
		headers.replaceAll(String::trim);
	}

	private class HeaderDetector {
		private boolean hasHeader;
		private String line;

		public HeaderDetector(String line) {
			this.line = line;
		}

		private boolean containsDigits() {
			return containsDigitsPattern.matcher(line).matches();
		}

		private boolean containsEmptyFields() {
			String tmp = line.replace(String.valueOf(stringSeparator), "");
			return Arrays.stream(tmp.split(String.valueOf(fieldSeparator))).anyMatch(String::isEmpty);
		}

		private boolean containsFieldsWithEmbeddedQuotes() {
			return line.contains(String.format("%c%c", stringSeparator, stringSeparator));
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

	public boolean hasExpectedHeaderNames(Collection<String> expectedHeaderNames) {
		return headers.stream().allMatch(header -> expectedHeaderNames.contains(header.trim()));
	}
}
