/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.text.Style;

public class LogFileParser {
	private String[] lines;
	private List<LogLine> parsedLogLines;
	private Style[] logLevelStyles;
	private List<String> typesList;
	private int maxExistingLevel = 1;
	private int minExistingLevel = 1;

	public LogFileParser(String[] lines, Style[] logLevelStyles) {
		this.lines = lines;
		this.logLevelStyles = logLevelStyles;
	}

	public LogLine getParsedLogLine(int index) {
		return parsedLogLines.get(index);
	}

	public List<LogLine> getParsedLogLines() {
		return Collections.unmodifiableList(parsedLogLines);
	}

	public List<String> getTypesList() {
		return Collections.unmodifiableList(typesList);
	}

	public int getMaxExistingLevel() {
		return maxExistingLevel;
	}

	public int getMinExistingLevel() {
		return minExistingLevel;
	}

	public void parse() {
		parsedLogLines = new ArrayList<>();
		typesList = new ArrayList<>();

		int lastKnownLogLevel = 0;
		for (int i = 0; i < lines.length; i++) {
			int logLevel = getLoglevelForLine(lines[i]);
			if (logLevel == 0) {
				logLevel = lastKnownLogLevel;
			} else {
				lastKnownLogLevel = logLevel;
			}
			parsedLogLines.add(
					new LogLine(i, logLevel, getTypeIndexForLine(lines[i]), getStyleByLevelNo(logLevel), lines[i]));
		}

		maxExistingLevel = IntStream.range(0, parsedLogLines.size()).map(i -> parsedLogLines.get(i).getLogLevel()).max()
				.getAsInt();
		minExistingLevel = IntStream.range(0, parsedLogLines.size()).map(i -> parsedLogLines.get(i).getLogLevel()).min()
				.getAsInt();
	}

	private static int getLoglevelForLine(String line) {
		int lineLevel = 0;
		if (line.length() >= 3 && line.charAt(0) == '[' && line.charAt(2) == ']') {
			lineLevel = Character.getNumericValue(line.charAt(1));
		}
		return Math.max(0, lineLevel);
	}

	private Style getStyleByLevelNo(int lev) {
		return lev < logLevelStyles.length ? logLevelStyles[lev] : logLevelStyles[logLevelStyles.length - 1];
	}

	private int getTypeIndexForLine(String line) {
		String type = "";
		int typeIndex = 0;
		int nextStartI = 0;
		StringBlock nextBlock = new StringBlock();
		nextBlock.setString(line);
		nextBlock.forward(nextStartI, '[', ']');
		if (nextBlock.hasFound()) {
			nextStartI = nextBlock.getIEnd() + 1;
			nextBlock.forward(nextStartI, '[', ']');
		}
		if (nextBlock.hasFound()) {
			nextStartI = nextBlock.getIEnd() + 1;
			nextBlock.forward(nextStartI, '[', ']');
		}
		if (nextBlock.hasFound()) {
			type = nextBlock.getContent();
			typeIndex = typesList.indexOf(type);
			if (typeIndex == -1) {
				typeIndex = typesList.size();
				typesList.add(type);
			}
		}
		return typeIndex;
	}
}
