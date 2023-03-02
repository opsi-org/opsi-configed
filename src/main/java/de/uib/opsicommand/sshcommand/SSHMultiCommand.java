package de.uib.opsicommand.sshcommand;

import java.util.List;

public interface SSHMultiCommand {
	List<SSHCommand> getCommands();

	List<String> getCommandsRaw();

	String getMainName();
}
