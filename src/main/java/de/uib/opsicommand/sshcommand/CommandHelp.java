package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;

import de.uib.configed.gui.FGeneralDialog;

public class CommandHelp /* extends */ implements SSHCommand // , SSHCommandNeedParameter

{
	// private String baseName = "";

	private boolean needSudo = false;

	private boolean needParameter = false;
	private SSHCommand basicCommand;
	protected FGeneralDialog dialog = null;

	public CommandHelp(SSHCommand basicCommand) {
		this.basicCommand = basicCommand;
		// command = this.basicCommand.getBasicName() + " --help";
		this.dialog = this.basicCommand.getDialog();
	}

	private boolean isMultiCommand = false;

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getId() {
		return basicCommand.getId();
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else
			return getCommand();
	}

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String get_ERROR_TEXT() {
		return "ERROR";
	}

	// @Override

	@Override
	public String getMenuText() {
		return null;
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public String getCommand() {
		return ((SSHCommandNeedParameter) this.basicCommand).getBasicName() + " --help";
	}

	@Override
	public void setCommand(String c) {
		// Leave empty, never used actually and never needed
	}

	@Override
	public String getCommandRaw() {
		return basicCommand.getCommandRaw();
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

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
		return 0;
	}

	@Override
	public ArrayList<String> getParameterList() {
		return null;
	}

	// @Override

	// regexes[0] = "(-[\\w*],[
	// ])|(--[\\w*]*-[\\w*]*-[\\w*]*)|(--[\\w*]*-[\\w*]*)|(--[\\w*]*)";
	// regexes[1] = "((<[\\w*]*>)|(<[\\w*]*-[\\w*]*>)|(<[\\w*]*-[\\w*]*-[\\w*]*>)) |
	

}