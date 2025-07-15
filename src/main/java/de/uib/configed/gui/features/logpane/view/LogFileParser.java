/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.text.Style;

import lombok.Getter;
import lombok.Setter;

public class LogFileParser {
	@Getter
	@Setter
	public static class LogParsedData {
		String[] lines;
		List<LogLine> parsedLogLines = new ArrayList<>();
		Style[] logLevelStyles;
		List<String> typesList = new ArrayList<>();
		int maxExistingLevel = 1;
		int minExistingLevel = 1;
	}

	private LogParsedData data;

	public LogFileParser(String[] lines, Style[] logLevelStyles) {
		data = new LogParsedData();
		data.setLines(lines);
		data.setLogLevelStyles(logLevelStyles);
	}

	public LogParsedData getData() {
		return data;
	}

	public void parse() {
		List<LogLine> parsedLogLines = new ArrayList<>();
		List<String> typesList = new ArrayList<>();
		typesList.add(LogTextPane.DEFAULT_TYPE);
		String[] lines = data.getLines();

		int lastKnownLogLevel = 0;
		for (int i = 0; i < lines.length; i++) {
			int logLevel = getLoglevelForLine(lines[i]);
			if (logLevel == 0) {
				logLevel = lastKnownLogLevel;
			} else {
				lastKnownLogLevel = logLevel;
			}
			parsedLogLines.add(new LogLine(i, logLevel, getTypeIndexForLine(lines[i], typesList),
					getStyleByLevelNo(logLevel), lines[i]));
		}

		int maxExistingLevel = IntStream.range(0, parsedLogLines.size()).map(i -> parsedLogLines.get(i).getLogLevel())
				.max().getAsInt();
		int minExistingLevel = IntStream.range(0, parsedLogLines.size()).map(i -> parsedLogLines.get(i).getLogLevel())
				.min().getAsInt();

		data.setParsedLogLines(parsedLogLines);
		data.setTypesList(typesList);
		data.setMaxExistingLevel(maxExistingLevel);
		data.setMinExistingLevel(minExistingLevel);
	}

	private static int getLoglevelForLine(String line) {
		int lineLevel = 0;
		if (line.length() >= 3 && line.charAt(0) == '[' && line.charAt(2) == ']') {
			lineLevel = Character.getNumericValue(line.charAt(1));
		}
		return Math.max(0, lineLevel);
	}

	private Style getStyleByLevelNo(int lev) {
		Style[] logLevelStyles = data.getLogLevelStyles();
		return lev < logLevelStyles.length ? logLevelStyles[lev] : logLevelStyles[logLevelStyles.length - 1];
	}

	private int getTypeIndexForLine(String line, List<String> typesList) {
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
