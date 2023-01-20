package de.uib.opsicommand.sshcommand;

import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHWgetParameterDialog;
import de.uib.utilities.logging.Logging;

public class CommandWget implements SSHCommand, SSHCommandNeedParameter {
	private String baseName = "wget ";
	private String command = "wget ";
	protected FGeneralDialog dialog = null;
	private boolean needSudo = false;
	private boolean needParameter = true;
	private boolean isMultiCommand = false;
	private int priority = 110;

	private String url = " ";
	private String authentication = " ";
	private String additional_url = " ";
	private String dir = " ";
	private String product = " ";
	private String filename = " ";
	private String verbosity = " ";
	private String freeInput = " ";

	public CommandWget() {
		command = "wget ";
	}

	public CommandWget(String d, String u, String au, String auth) {
		this(d, u, au);
		setAuthentication(auth);
	}

	public CommandWget(String d, String u, String au) {
		this(d, u);
		additional_url = au;
	}

	public CommandWget(String d, String u) {
		setVerbosity(1);
		setDir(d);
		setUrl(u);

		if (d.charAt(d.length() - 1) != '/')
			d = d + "/";
		setProduct(d + getFilenameFromUrl(url));
		Logging.debug(this, "CommandWget dir " + dir);
		Logging.debug(this, "CommandWget url " + url);
		Logging.debug(this, "CommandWget product " + getProduct());
		needParameter = false;
	}

	public void setFilename(String newFilename) {
		if ((newFilename != null) && (!newFilename.trim().equals("")))
			filename = " --output-document=" + newFilename + " ";
	}

	@Override
	public String get_ERROR_TEXT() {
		return "ERROR";
	}

	public void setAuthentication(String a) {
		if (a != null)
			authentication = a;
	}

	public String getAuthentication() {
		return authentication;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getId() {
		return "CommandWget";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.wget");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.wget.tooltip");
	}

	@Override
	public String getCommand() {
		if (!freeInput.equals(""))
			command = "wget " + authentication + filename + freeInput + verbosity + dir + url + " " + additional_url;
		else
			command = "wget " + authentication + filename + verbosity + dir + url + " " + additional_url;
		if (needSudo())
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		return command + " 2>&1";
	}

	@Override
	public String getSecureInfoInCommand() {
		return authentication;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		else
			return getCommand();
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
		return priority;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHWgetParameterDialog();
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHWgetParameterDialog(main);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command);
		return exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setDir(String d) {
		if (!d.equals(""))
			dir = " -P " + d;
		else
			dir = "";
	}

	public void setUrl(String u) {
		if (!u.equals(""))
			url = " " + u;
		else
			url = "";
	}

	public void setVerbosity(int vSum) {
		String v = "";
		for (int i = 0; i < vSum; i++)
			v = v + "v";
		verbosity = " -" + v + " ";
		if (vSum == 0)
			verbosity = "";
	}

	public void setFreeInput(String fI) {
		freeInput = " " + fI + " ";
	}

	public void setProduct(String pr) {
		product = " " + pr;
	}

	public String getProduct() {
		return product;
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

	public boolean checkCommand() {
		return dir.equals("") && url.equals("");
	}

	private String getFilenameFromUrl(String url) {
		int p = url.lastIndexOf("/");
		return url.substring(p + 1);
	}

	@Override
	public List<String> getParameterList() {
		return null;
	}
}
