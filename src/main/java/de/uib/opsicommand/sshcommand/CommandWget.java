package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHWgetParameterDialog;
import de.uib.utilities.logging.Logging;

public class CommandWget implements SSHCommand, SSHCommandNeedParameter {
	private static final String BASE_NAME = "wget ";
	private static final boolean NEED_SUDO = false;
	private static final boolean IS_MULTI_COMMAND = false;
	private static final int PRIORITY = 110;

	private String command = "wget ";
	private boolean needParameter = true;

	private String url = " ";
	private String authentication = " ";
	private String additionalURL = " ";
	private String dir = " ";
	private String product = " ";
	private String fileName = " ";
	private String verbosity = " ";
	private String freeInput = " ";

	private FGeneralDialog dialog;

	public CommandWget() {
	}

	public CommandWget(String d, String u, String au, String auth) {
		this(d, u, au);
		setAuthentication(auth);
	}

	public CommandWget(String d, String u, String au) {
		this(d, u);
		additionalURL = au;
	}

	public CommandWget(String d, String u) {
		setVerbosity(1);
		setDir(d);
		setUrl(u);

		if (d.charAt(d.length() - 1) != '/') {
			d = d + "/";
		}
		setProduct(d + getFilenameFromUrl(url));
		Logging.debug(this, "CommandWget dir " + dir);
		Logging.debug(this, "CommandWget url " + url);
		Logging.debug(this, "CommandWget product " + getProduct());
		needParameter = false;
	}

	public void setFileName(String newFilename) {
		if ((newFilename != null) && (!newFilename.trim().isEmpty())) {
			fileName = " --output-document=" + newFilename + " ";
		}
	}

	@Override
	public String getErrorText() {
		return "ERROR";
	}

	public void setAuthentication(String a) {
		if (a != null) {
			authentication = a;
		}
	}

	@Override
	public boolean isMultiCommand() {
		return IS_MULTI_COMMAND;
	}

	@Override
	public String getId() {
		return "CommandWget";
	}

	@Override
	public String getBasicName() {
		return BASE_NAME;
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
		if (!freeInput.isEmpty()) {
			command = "wget " + authentication + fileName + freeInput + verbosity + dir + url + " " + additionalURL;
		} else {
			command = "wget " + authentication + fileName + verbosity + dir + url + " " + additionalURL;
		}
		if (needSudo()) {
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		}
		return command + " 2>&1";
	}

	@Override
	public String getSecureInfoInCommand() {
		return authentication;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().isEmpty())) {
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public boolean needSudo() {
		return NEED_SUDO;
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
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHWgetParameterDialog();
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand commandHelp = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(commandHelp);
		return exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setDir(String d) {
		if (!d.isEmpty()) {
			dir = " -P " + d;
		} else {
			dir = "";
		}
	}

	public void setUrl(String u) {
		if (!u.isEmpty()) {
			url = " " + u;
		} else {
			url = "";
		}
	}

	public void setVerbosity(int vSum) {
		StringBuilder v = new StringBuilder();
		for (int i = 0; i < vSum; i++) {
			v.append("v");
		}

		verbosity = " -" + v + " ";
		if (vSum == 0) {
			verbosity = "";
		}
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
		return dir.isEmpty() && url.isEmpty();
	}

	private static String getFilenameFromUrl(String url) {
		int p = url.lastIndexOf("/");
		return url.substring(p + 1);
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
