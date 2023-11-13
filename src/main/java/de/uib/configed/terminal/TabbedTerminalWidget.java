/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import javax.swing.JTabbedPane;

public class TabbedTerminalWidget extends JTabbedPane {
	TerminalFrame terminalFrame;

	public TabbedTerminalWidget(TerminalFrame terminalFrame) {
		this.terminalFrame = terminalFrame;
	}
}
