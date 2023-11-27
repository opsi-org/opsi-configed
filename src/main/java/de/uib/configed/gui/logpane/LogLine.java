/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import javax.swing.text.Style;

public class LogLine {
	private int level;
	private int typeIndex;
	private Style style;

	public LogLine(int level, int typeIndex, Style style) {
		this.level = level;
		this.typeIndex = typeIndex;
		this.style = style;
	}

	public int getLevel() {
		return level;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public Style getStyle() {
		return style;
	}
}
