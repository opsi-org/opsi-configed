/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.io.File;
import java.util.List;

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.TerminalFrame;

public class TerminalCommandExecutor {
	private ConfigedMain configedMain;

	public TerminalCommandExecutor(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void execute(TerminalCommand command) {
		TerminalFrame terminalFrame = new TerminalFrame(true);
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		terminalFrame.display();
		terminalFrame.disableUserInputForSelectedWidget();
		if (command instanceof TerminalMultiCommand) {
			executeMultiCommand(terminalFrame, (TerminalMultiCommand) command);
		} else {
			terminalFrame.execute(command.getCommand());
		}
	}

	private static void executeMultiCommand(TerminalFrame terminalFrame, TerminalMultiCommand multiCommand) {
		List<TerminalCommand> commands = multiCommand.getCommands();
		StringBuilder multiCommandsCombined = new StringBuilder();
		for (int i = 0; i < commands.size(); i++) {
			TerminalCommand currentCommand = commands.get(i);
			if (currentCommand instanceof TerminalCommandFileUpload) {
				TerminalCommandFileUpload fileUploadCommand = (TerminalCommandFileUpload) currentCommand;
				terminalFrame.uploadFile(new File(fileUploadCommand.getFullSourcePath()),
						fileUploadCommand.getTargetPath());
			} else {
				multiCommandsCombined.append(currentCommand.getCommand());
				if (commands.size() != i + 1) {
					multiCommandsCombined.append(" && ");
				}
			}
		}
		if (!multiCommandsCombined.isEmpty()) {
			terminalFrame.execute(multiCommandsCombined.toString());
		}
	}
}
