package de.uib.configed.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.uib.configed.csv.exceptions.CSVException;
import de.uib.configed.csv.exceptions.CSVFieldCountException;
import de.uib.configed.csv.exceptions.CSVParserException;

public class CSVParser {
	private static final CSVFormat DEFAULT_FORMAT = new CSVFormat();
	private static final boolean DEFAULT_IGNORE_ERRORS = false;

	private String pendingField = "";
	private CSVFormat format;

	private boolean inQuotes = false;
	public boolean isMultiLine = false;

	private boolean ignoreErrors = false;

	private int pendingFieldCount = 0;
	private List<Integer> numberOfFieldsPerLine = new ArrayList<>();

	public CSVParser() {
		this(DEFAULT_FORMAT, DEFAULT_IGNORE_ERRORS);
	}

	public CSVParser(CSVFormat format) {
		this(format, DEFAULT_IGNORE_ERRORS);
	}

	public CSVParser(boolean ignoreErrors) {
		this(DEFAULT_FORMAT, ignoreErrors);
	}

	public CSVParser(CSVFormat format, boolean ignoreErrors) {
		this.format = format;
		this.ignoreErrors = ignoreErrors;
	}

	public String[] parseLine(List<CSVToken> tokens) throws CSVException {
		return parse(tokens).stream().toArray(String[]::new);
	}

	public boolean isMultiLine(String line) {
		if (line == null) {
			return false;
		}

		for (char c : line.toCharArray()) {
			if (c == format.getStringSeparator())
				inQuotes = !inQuotes;
		}

		return inQuotes;
	}

	public List<String> parse(List<CSVToken> tokens) throws CSVException {
		List<String> result = new ArrayList<>();
		int fieldCount = (pendingFieldCount != 0) ? pendingFieldCount : 0;
		pendingFieldCount = 0;

		StringBuilder field = new StringBuilder();
		if (!pendingField.isEmpty()) {
			field.append(pendingField);
			pendingField = "";
		}

		for (int i = 0; i < tokens.size(); i++) {
			CSVToken token = tokens.get(i);

			switch (token.getName()) {
			case CSVToken.STRING_SEPARATOR:
				if (ignoreErrors || i == 0) {
					break;
				}

				boolean fieldStart = false;
				boolean fieldEnd = false;

				if (tokens.get(i - 1).equals(CSVToken.FIELD_SEPARATOR)
						&& (tokens.get(i + 1).equals(CSVToken.FIELD) || tokens.get(i + 1).equals(CSVToken.NEW_LINE)
								|| tokens.get(i + 1).equals(CSVToken.EMBEDDED_QUOTE))) {
					fieldStart = true;
				}

				if (((tokens.get(i - 1).equals(CSVToken.FIELD) || tokens.get(i - 1).equals(CSVToken.EMBEDDED_QUOTE))
						&& (tokens.get(i + 1).equals(CSVToken.FIELD_SEPARATOR)
								|| tokens.get(i + 1).equals(CSVToken.LINE_END)))) {
					fieldEnd = true;
				}

				if (!fieldStart && !fieldEnd) {
					throw new CSVParserException("Syntax error occurred");
				}
				break;
			case CSVToken.NEW_LINE:
				pendingFieldCount = fieldCount;
				field.append(token.getValue());
				pendingField = field.toString();
				isMultiLine = true;
				break;
			case CSVToken.FIELD_SEPARATOR:
				fieldCount++;
				result.add(field.toString());
				field.setLength(0);
				break;
			case CSVToken.EMPTY_FIELD:
				break;
			case CSVToken.FIELD:
				field.append(token.getValue());
				isMultiLine = false;
				break;
			case CSVToken.EMBEDDED_QUOTE:
				field.append(token.getValue());
				break;
			case CSVToken.LINE_END:
				fieldCount++;
				result.add(field.toString());
				field.setLength(0);

				if (fieldCount != 0) {
					numberOfFieldsPerLine.add(fieldCount);
					fieldCount = 0;
				}
				break;
			}
		}

		if (!ignoreErrors && !equalFields()) {
			throw new CSVFieldCountException("Unequal fields");
		}

		return result;
	}

	private boolean equalFields() {
		if (numberOfFieldsPerLine.isEmpty()) {
			return true;
		}

		long commonFieldCount = numberOfFieldsPerLine.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
				.max(Map.Entry.comparingByValue()).get().getKey();

		return pendingFieldCount != 0
				|| numberOfFieldsPerLine.stream().allMatch(lineFieldCount -> lineFieldCount == commonFieldCount);
	}

	public CSVFormat getFormat() {
		return format;
	}

	public void setFormat(CSVFormat format) {
		this.format = format;
	}

	public boolean getIgnoreErrors() {
		return ignoreErrors;
	}

	public void setIgnoreErrors(boolean ignoreErrors) {
		this.ignoreErrors = ignoreErrors;
	}

	public boolean getIsMultiLine() {
		return isMultiLine;
	}
}
