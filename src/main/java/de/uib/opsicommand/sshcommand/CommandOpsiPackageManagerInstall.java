package de.uib.opsicommand.sshcommand;

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.utilities.logging.*;

public class CommandOpsiPackageManagerInstall extends CommandOpsiPackageManager implements SSHCommandNeedParameter
{
	private String command;
	private boolean install;
	private int priority= 8;
	protected FGeneralDialog dialog;
	private boolean isMultiCommand = false;

	String opsiproduct = "";
	String depot = "";
	String verbosity = "-vvv";
	String freeInput = "";
	String property = " -p keep ";

	String updateInstalled = "";
	String setupInstalled = "";
	public CommandOpsiPackageManagerInstall()
	{
		command = "opsi-package-manager";
	}

	@Override
	public String getId()
	{
		return "CommandOpsiPackageManagerInstall";
	}

	@Override
	public String getBasicName()
	{
		return "opsi-package-manager";
	}

	@Override
	public String getMenuText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_install");
	}

	@Override
	public String getParentMenuText()
	{
		return super.getMenuText();
	}

	@Override
	public String getToolTipText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_install.tooltip");
	}

	@Override
	public boolean isMultiCommand()
	{
		return isMultiCommand;
	}

	@Override
	public String getCommand()
	{
		command = "opsi-package-manager  --force -q " + verbosity + updateInstalled  + setupInstalled + property + depot + freeInput + opsiproduct;
		logging.info(this, "got command " + command);
		if (needSudo())	return SSHCommandFactory.getInstance().sudo_text +" "+ command + " 2>&1";
		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw()
	{
		return command;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void startParameterGui()
	{
		dialog = new SSHPackageManagerInstallParameterDialog();
	}

	@Override
	public void startParameterGui(ConfigedMain main)
	{
		dialog = new SSHPackageManagerInstallParameterDialog(main);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog()
	{
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec( command);
		return (SSHConnectionExecDialog) exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog()
	{
		return dialog;
	}

	public void setOpsiproduct(String prod)
	{
		if (prod != "") opsiproduct = " -i " + prod;
		else opsiproduct = "";
	}

	public void setDepotForPInstall(String dep)
	{
		if (dep != "")
			depot = " -d " + dep;
		else depot = "";
	}

	public void setVerbosity(int v_sum)
	{
		String v = "v";
		for (int i = 0; i < v_sum; i++) 
			v = v + "v";
		verbosity = " -" + v + " ";
	}

	public void setFreeInput(String fI)
	{
		freeInput = " " + fI ;
	}

	public boolean checkCommand()
	{
		if (opsiproduct == "") return false;
		return true;
	}

	public void setProperty(boolean keep_depot_defaults)
	{
		if (keep_depot_defaults)
			property = " -p keep ";
		else
			property = " -p package ";
	}

	public String getProperty()
	{
		return property;
	}

	public void setUpdateInstalled(boolean u)
	{
		if (u) updateInstalled = " --update ";
		else updateInstalled = "";
	}
	
	public void setSetupInstalled(boolean s)
	{
		if (s) setupInstalled = " --setup ";
		else setupInstalled = "";
	}
}