package de.uib.opsicommand.sshcommand;

import java.util.LinkedList;

public interface SSHMultiCommand {
	public LinkedList<SSHCommand> getCommands();

	public LinkedList<String> getCommandsRaw();

	public String getMainName();
}
