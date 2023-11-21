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
	private static final Pattern doubleMinusPattern = Pattern.compile("--");

	private static final char DEFAULT_FIELD_SEPARATOR = ',';
	private static final char DEFAULT_STRING_SEPARATOR = '"';
	private static final String FORMAT_HINT_INDICATOR = "#";

	private char delimiter = DEFAULT_FIELD_SEPARATOR;
	private char quote = DEFAULT_STRING_SEPARATOR;
	private boolean hasHeader;
	private boolean hasHint;

	private Map<Character, Integer> possibleDelimiters = new HashMap<>();
	private Map<Character, Long> possibleQuotes = new HashMap<>();

	private List<String> headers;

	public char getDelimiter() {
		return this.delimiter;
	}

	public void setDelimiter(char fieldSeparator) {
		this.delimiter = fieldSeparator;
	}

	public char getQuote() {
		return quote;
	}

	public void setQuote(char stringSeparator) {
		this.quote = stringSeparator;
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

			delimiter = formatOptions[0].charAt(formatOptions[0].indexOf("=") + 1);
			quote = formatOptions[1].charAt(formatOptions[1].indexOf("=") + 1);
		} else {
			hasHint = false;

			detectQoute(fileAsString);
			detectDelimiter(fileAsList);
		}

		detectHeader(fileAsList);
	}

	private void detectDelimiter(List<String> fileAsList) {
		boolean inQuotes = false;

		for (int i = 0; i < fileAsList.size(); i++) {
			String line = fileAsList.get(i);

			for (char c : line.toCharArray()) {
				if (c == quote) {
					inQuotes = !inQuotes;
					continue;
				}

				if (!Character.isLetter(c) && !Character.isDigit(c) && !inQuotes) {
					possibleDelimiters.put(c, possibleDelimiters.containsKey(c) ? (possibleDelimiters.get(c) + 1) : 1);
				}
			}
		}

		delimiter = Collections.max(possibleDelimiters.entrySet(), Entry.comparingByValue()).getKey();
	}

	private void detectQoute(String fileAsString) {
		char singleQuote = '\'';
		char doubleQuote = '"';

		long singleQuoteCount = fileAsString.chars().filter(ch -> ch == singleQuote).count();
		long doubleQuoteCount = fileAsString.chars().filter(ch -> ch == doubleQuote).count();

		possibleQuotes.put(singleQuote, singleQuoteCount);
		possibleQuotes.put(doubleQuote, doubleQuoteCount);

		quote = Collections.max(possibleQuotes.entrySet(), Entry.comparingByValue()).getKey();
	}

	private void detectHeader(List<String> fileAsList) {
		int lineNumber = 0;
		if (fileAsList.get(0).startsWith(FORMAT_HINT_INDICATOR)) {
			lineNumber = 1;
		}

		CSVHeaderDetector csvHeaderDetector = new CSVHeaderDetector(fileAsList.get(lineNumber), this);
		hasHeader = csvHeaderDetector.detect();
		String header = csvHeaderDetector.getHeader();
		headers = Arrays.asList(header.replace(String.valueOf(quote), "").split(String.valueOf(delimiter)));
		headers.replaceAll(String::trim);
	}

	public boolean hasExpectedHeaderNames(Collection<String> expectedHeaderNames) {
		return headers.stream().allMatch(header -> expectedHeaderNames.contains(header.trim()));
	}
}
