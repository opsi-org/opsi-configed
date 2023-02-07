package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.gui.FGeneralDialog;

public class CommandOpsiPackageManager implements SSHCommand {

	protected LinkedList<Object> helpLinesSplitted;
	protected FGeneralDialog dialog = null;
	protected boolean needSudo = false;
	protected boolean needRoot = false;
	protected boolean needParameter = true;
	private boolean isMultiCommand = false;
	protected int helpColumns = 3;
	protected int priority = 100;

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String getErrorText() {
		return "ERROR";
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		else
			return getCommand();
	}

	@Override
	public String getId() {
		return "CommandOpsiPackageManager";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager.tooltip");
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getCommand() {
		return "";
	}

	@Override
	public String getCommandRaw() {
		return "";
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

	/**
	 * Sets the given command
	 * 
	 * @param c (command): String
	 **/
	@Override
	public void setCommand(String c) {
		/* Not needed in this class */}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}