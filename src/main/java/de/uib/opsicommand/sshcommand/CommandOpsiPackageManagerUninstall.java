package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHPackageManagerUninstallParameterDialog;
import de.uib.utilities.logging.logging;

public class CommandOpsiPackageManagerUninstall extends CommandOpsiPackageManager implements SSHCommandNeedParameter {
	protected FGeneralDialog dialog = null;
	private String command;
	private int priority = 10;
	// private boolean isMultiCommand = false;
	String opsiproduct;
	String depots;
	String verbosity = " -vvv ";
	String keepFiles = " ";
	String freeInput = " ";

	public CommandOpsiPackageManagerUninstall() {
		command = "opsi-package-manager";
	}

	@Override
	public String getId() {
		return "CommandOpsiPackageManagerUninstall";
	}

	@Override
	public String getMenuText() {
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall");
	}

	@Override
	public String getParentMenuText() {
		// return "Package-Manager";
		return super.getMenuText();
	}

	@Override
	public String getBasicName() {
		return "opsi-package-manager";
	}

	@Override
	public String getToolTipText() {
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall.tooltip");
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHPackageManagerUninstallParameterDialog();
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHPackageManagerUninstallParameterDialog(main);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command
		// SSHConnectionExecDialog.getInstance(
		// configed.getResourceValue("SSHConnection.Exec.title") + "
		// \""+command.getCommand() + "\" ",
		// command
		// )
		);

		// SSHConnectExec exec = new SSHConnectExec();
		// exec.exec(command, true, new SSHConnectionExecDialog(command,
		// configed.getResourceValue("SSHConnection.Exec.title") + "
		// \""+command.getCommand() + "\" "));
		return (SSHConnectionExecDialog) exec.getDialog();
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public String getCommand() {
		command = "opsi-package-manager -q " + verbosity + keepFiles + depots + freeInput + opsiproduct;
		if (needSudo())
			return SSHCommandFactory.getInstance().sudo_text + " " + command + " 2>&1";
		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	public void setKeepFiles(boolean kF) {
		if (kF)
			keepFiles = "  --keep-files ";
		else
			keepFiles = "";
	}

	public void setOpsiproduct(String prod) {
		if (prod != null && !prod.equals(""))
			opsiproduct = " -r " + prod;
		else
			opsiproduct = " ";
	}

	public void setDepot(String depotlist) {
		if (depotlist != null && !depotlist.equals(""))
			depots = " -d " + depotlist;
		else
			depots = " ";
	}

	public void setVerbosity(int v_sum) {
		String v = "v";
		for (int i = 0; i < v_sum; i++)
			v = v + "v";
		verbosity = " -" + v + " ";
	}

	public void setFreeInput(String fI) {
		freeInput = " " + fI;
	}

	public boolean checkCommand() {
		if (opsiproduct == null || opsiproduct.trim().equals("")) {
			logging.info(this, "no product given");
			return false;
		}
		return true;
	}

}