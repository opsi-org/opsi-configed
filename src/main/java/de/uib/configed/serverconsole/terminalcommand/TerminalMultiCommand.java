package de.uib.configed.serverconsole.terminalcommand;

import java.util.List;

public interface TerminalMultiCommand extends TerminalCommandMetadata {
	List<TerminalSingleCommand> getCommands();

	List<String> getCommandsRaw();

	String getMainName();
}
