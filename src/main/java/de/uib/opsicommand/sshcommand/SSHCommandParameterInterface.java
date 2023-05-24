package de.uib.opsicommand.sshcommand;

/**
 * This interface handles SSHCommands.
 **/
public interface SSHCommandParameterInterface {

	String[] getSelectedClientNames();

	String[] getSelectedDepotNames();

	String getConfigServerName();

	String getConfigSSHServerName();
}
