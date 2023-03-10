package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHMakeProductFileDialog;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class CommandOpsimakeproductfile implements SSHCommand, SSHCommandNeedParameter {
	private String baseName = "opsi-makeproductfile ";
	private String command = "opsi-makeproductfile ";

	protected FGeneralDialog dialog = null;
	private boolean needSudo = false;
	private boolean needParameter = true;
	private boolean isMultiCommand = false;
	private static final int PRIORITY = 110;

	private String dir = " ";
	private String keepVersions = " ";
	private String packageVersion = " ";
	private String productVersion = " ";
	private String md5sum = " -m ";
	private String zsync = " -z ";

	public CommandOpsimakeproductfile(String d, String pav, String prv, boolean m, boolean z) {
		setDir(d);
		setPackageVersion(pav);
		setProductVersion(prv);
		setMd5sum(m);
		setZsync(z);
		Logging.info(this, "CommandOpsimakeproductfile dir " + dir);
		Logging.info(this, "CommandOpsimakeproductfile packageVersion " + packageVersion);
		Logging.info(this, "CommandOpsimakeproductfile productVersion " + productVersion);
	}

	public CommandOpsimakeproductfile(String d, String pav, String prv) {
		this(d, pav, prv, false, false);
	}

	public CommandOpsimakeproductfile(String d, boolean o, boolean m, boolean z) {
		setDir(d);
		setKeepVersions(o);
		setMd5sum(m);
		setZsync(z);
	}

	public CommandOpsimakeproductfile() {
	}

	/**
	 * check persistenceController opsi version and set Command name dependend
	 * on this version to 'opsi-makeproductfile' or 'opsi-makepackage'
	 */
	private void setCommandName() {
		if (!PersistenceControllerFactory.getPersistenceController().handleVersionOlderThan("4.1")) {
			Logging.info(this, "set CommandOpsimakeproductfile name to 'opsi-makepackage'");
			baseName = "opsi-makepackage";
			command = "opsi-makepackage";
		} else {
			Logging.info(this, "let CommandOpsimakeproductfile named as 'opsi-makeproductfile'");
		}
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals(""))) {
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String getErrorText() {
		return "ERROR";
	}

	private void setDir(String d) {
		if (d != null) {
			dir = d;
		}
	}

	public String getDir() {
		return dir;
	}

	private void setKeepVersions(boolean o) {
		if (o) {
			keepVersions = " --keep-versions ";
		} else {
			keepVersions = " ";
		}
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getId() {
		return "CommandMakeproductfile";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.tooltip");
	}

	@Override
	public String getCommand() {
		setCommandName();
		if (!packageVersion.equals("") || !productVersion.equals("")) {
			keepVersions = "--keep-versions ";
		}

		command = "cd " + dir + " && " + baseName + " " + keepVersions + " " + packageVersion + " " + productVersion
				+ " " + md5sum + " " + zsync + " ";
		if (needSudo()) {
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		}

		return command + " 2>&1";
	}

	/**
	 * Sets the given command
	 * 
	 * @param c (command): String
	 **/
	@Override
	public void setCommand(String c) {
		command = c;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHMakeProductFileDialog(null);
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHMakeProductFileDialog(main);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command
		// SSHConnectionExecDialog.getInstance(

		// \""+command.getCommand() + "\" ",
		// command
		// )
		);
		return exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	private void setMd5sum(boolean m) {
		if (m) {
			md5sum = "-m";
		} else {
			md5sum = "";
		}
	}

	private void setZsync(boolean z) {
		if (z) {
			zsync = "-z";
		} else {
			zsync = "";
		}
	}

	private void setPackageVersion(String pav) {
		if (!pav.equals("")) {
			packageVersion = "--package-version " + pav;
		} else {
			packageVersion = "";
		}
	}

	private void setProductVersion(String prv) {
		if (!prv.equals("")) {
			productVersion = "--product-version " + prv;
		} else {
			productVersion = "";
		}
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
