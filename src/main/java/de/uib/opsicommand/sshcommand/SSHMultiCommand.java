package de.uib.opsicommand.sshcommand;

import java.util.List;

public interface SSHMultiCommand {
	public List<SSHCommand> getCommands();

	public List<String> getCommandsRaw();

	public String getMainName();
}
