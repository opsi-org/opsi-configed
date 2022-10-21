package de.uib.opsicommand.sshcommand;
import java.util.*;
public interface SSHMultiCommand
{
	public LinkedList<SSHCommand> getCommands();
	public LinkedList<String> getCommandsRaw();
	// public int getCommandsCount();
	public String getMainName();
}
