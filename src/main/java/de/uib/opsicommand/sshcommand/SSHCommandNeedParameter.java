package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;

public interface SSHCommandNeedParameter {
	void startParameterGui();

	void startParameterGui(ConfigedMain main);

	SSHConnectionExecDialog startHelpDialog();

	String getBasicName();
}
