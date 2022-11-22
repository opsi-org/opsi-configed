package de.uib.configed.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.uib.configed.csv.exceptions.CSVException;

public class CSVReader {
	private static final CSVParser DEFAULT_PARSER = new CSVParser();
	private static final int DEFAULT_START_LINE = 0;

	private BufferedReader reader;
	private int startLine;
	private List<String> headerNames;

	private CSVFormat format;
	private CSVScanner scanner;
	private CSVParser parser;

	public CSVReader(Reader reader) {
		this(reader, DEFAULT_PARSER, DEFAULT_START_LINE, null);
	}

	public CSVReader(Reader reader, int startLine) {
		this(reader, DEFAULT_PARSER, startLine, null);
	}

	public CSVReader(Reader reader, CSVParser parser) {
		this(reader, parser, DEFAULT_START_LINE, null);
	}

	public CSVReader(Reader reader, CSVParser parser, int startLine) {
		this(reader, parser, startLine, null);
	}

	public CSVReader(Reader reader, CSVParser parser, int startLine, List<String> headerNames) {
		this.reader = (reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader));
		this.parser = parser;
		this.format = parser.getFormat();
		this.scanner = new CSVScanner(this.reader, format);
		this.startLine = startLine;
		this.headerNames = headerNames;
	}

	public List<Map<String, Object>> readAll() throws CSVException, IOException {
		List<Map<String, Object>> result = new ArrayList<>();
		String[] pendingLine = new String[] {};

		if (format.hasHint()) {
			reader.readLine();
			startLine -= 1;
		}

		if (startLine != DEFAULT_START_LINE) {
			skipTo(startLine);
			startLine = DEFAULT_START_LINE;
		}

		scanner.scan();
		List<List<CSVToken>> tokens = scanner.getTokens();

		for (List<CSVToken> lineOfCSVToken : tokens) {
			String[] parsedLine = parser.parseLine(lineOfCSVToken);

			if (parser.isMultiLine) {
				pendingLine = joinArrays(pendingLine, parsedLine);
			} else {
				if (pendingLine != null && pendingLine.length != 0) {
					parsedLine = joinArrays(pendingLine, parsedLine);
					result.add(createLineAsMap(parsedLine));
					pendingLine = new String[] {};
				} else {
					result.add(createLineAsMap(parsedLine));
				}
			}
		}

		return result;
	}

	private Map<String, Object> createLineAsMap(String[] csvLine) {
		List<String> headers = headerNames != null ? headerNames : format.getHeaders();
		Map<String, Object> lineAsMap = new TreeMap<>();

		for (int i = 0; i < csvLine.length; i++) {
			if (i >= headers.size()) {
				break;
			}
			lineAsMap.put(headers.get(i), csvLine[i]);
		}

		return lineAsMap;
	}

	public void skipTo(int startLine) throws IOException {
		for (int i = 1; i < startLine; i++) {
			String line = reader.readLine();

			if (parser.isMultiLine(line)) {
				startLine++;
			}
		}
	}

	public void close() throws IOException {
		reader.close();
	}

	private String[] joinArrays(String[] array1, String[] array2) {
		return Stream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray(String[]::new);
	}
}
