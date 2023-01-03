package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;

public interface SSHCommandNeedParameter {
	public void startParameterGui();

	public void startParameterGui(ConfigedMain main);

	public SSHConnectionExecDialog startHelpDialog();

	public String getBasicName();

}
