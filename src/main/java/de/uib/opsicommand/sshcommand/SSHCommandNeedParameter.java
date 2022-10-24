package de.uib.opsicommand.sshcommand;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;

public interface SSHCommandNeedParameter
{
	public void startParameterGui();
	public void startParameterGui(ConfigedMain main);
	public SSHConnectionExecDialog startHelpDialog();
	public String getBasicName();
	// public void setVisible();
}
