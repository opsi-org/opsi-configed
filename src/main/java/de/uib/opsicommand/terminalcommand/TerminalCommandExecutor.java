/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.TerminalFrame;

public class TerminalCommandExecutor {
	private ConfigedMain configedMain;

	public TerminalCommandExecutor(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void execute(TerminalCommand command) {
		TerminalFrame terminalFrame = new TerminalFrame();
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		terminalFrame.display();
		terminalFrame.execute(command.getCommand());
	}
}
