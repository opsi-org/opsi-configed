package de.uib.configed.serverconsole.terminalcommand;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

public interface TerminalSingleCommand extends TerminalCommandMetadata {
	String getCommand();

	String getSecuredCommand();

	String getSecureInfoInCommand();

	String getCommandRaw();

	List<String> getParameterList();

	void setCommand(String c);

	boolean needParameter();

	FGeneralDialog getDialog();
}
