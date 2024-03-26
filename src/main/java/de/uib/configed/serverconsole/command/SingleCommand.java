package de.uib.configed.serverconsole.command;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

public interface SingleCommand extends CommandMetadata {
	String getCommand();

	String getSecuredCommand();

	String getSecureInfoInCommand();

	String getCommandRaw();

	List<String> getParameterList();

	void setCommand(String c);

	boolean needParameter();

	FGeneralDialog getDialog();
}
