/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import javax.swing.text.Style;

public class LogLine {
	private int lineNumber;
	private int logLevel;
	private int typeIndex;
	private Style style;
	private String text;

	public LogLine(int lineNumber, int logLevel, int typeIndex, Style style, String text) {
		this.lineNumber = lineNumber;
		this.logLevel = logLevel;
		this.typeIndex = typeIndex;
		this.style = style;
		this.text = text;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getLogLevel() {
		return logLevel;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public Style getStyle() {
		return style;
	}

	public String getText() {
		return text;
	}
}
