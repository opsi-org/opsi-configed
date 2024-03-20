/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.utilities.logging.Logging;

public class TerminalMultiCommand implements TerminalCommand, Comparable<TerminalMultiCommand> {
	private static final String CONFIDENTIAL_INFORMATION = null;

	private boolean needParameter;
	private boolean isMultiCommand = true;
	private String id;
	private String menuText;
	private List<TerminalCommand> commands = new LinkedList<>();
	private List<TerminalCommand> sshCommandOriginal = new LinkedList<>();
	private String parentMenuText;
	private String tooltipText = "";
	private int position;

	private String mainName = "";

	private boolean firstInitCommands = true;

	public TerminalMultiCommand() {
		position = TerminalCommandFactory.DEFAULT_POSITION;
	}

	public TerminalMultiCommand(String id, List<String> c, String mt, String pmt, String ttt, int p) {
		position = TerminalCommandFactory.DEFAULT_POSITION;

		initValues(id, c, mt, pmt, ttt, p);
	}

	public TerminalMultiCommand(SSHCommand orig, List<String> commandlist) {
		this(orig.getId(), commandlist, orig.getMenuText(), orig.getParentMenuText(), orig.getToolTipText(),
				orig.getPriority());
	}

	public TerminalMultiCommand(TerminalMultiCommand orig) {
		this(orig.getId(), orig.getCommandsRaw(), orig.getMenuText(), orig.getParentMenuText(), orig.getToolTipText(),
				orig.getPriority());
	}

	private void initValues(String id, List<String> c, String mt, String pmt, String ttt, int p) {
		setId(id);
		setMenuText(mt);
		setParentMenuText(pmt);
		setTooltipText(ttt);
		setPriority(p);
		setCommands(c);
		Logging.debug(this, "SSHCommand_Template this " + this.toString());
		Logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}

	public String getMainName() {
		return mainName;
	}

	public void setMainName(String n) {
		mainName = n;
	}

	public void setId(String i) {
		id = i;
	}

	public void setCommands(List<String> cList) {
		if (cList != null) {
			commands.clear();
			for (String c : cList) {
				TerminalCommand sshc = new TerminalEmptyCommand(getId(), c, getMenuText());
				commands.add(sshc);
				if (firstInitCommands) {
					sshCommandOriginal.add(sshc);
				}
			}
			firstInitCommands = false;
		}
	}

	@Override
	public void setCommand(String c) {
		/* Not needed here */
	}

	public void addCommand(TerminalCommand sshc) {
		commands.add(sshc);
		sshCommandOriginal.add(sshc);
	}

	@Override
	public String getSecureInfoInCommand() {
		return CONFIDENTIAL_INFORMATION;
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isEmpty()) {
			return getCommand().replace(getSecureInfoInCommand(), TerminalCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	public void setMenuText(String mt) {
		menuText = mt;
	}

	public void setParentMenuText(String pmt) {
		parentMenuText = pmt;
	}

	public void setTooltipText(String ttt) {
		tooltipText = ttt;
	}

	public void setPriority(int p) {
		position = p;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getMenuText() {
		if (menuText != null && menuText.length() > 0) {
			return menuText.trim();
		}

		return menuText;
	}

	@Override
	public String getParentMenuText() {
		return parentMenuText;
	}

	@Override
	public String getToolTipText() {
		return tooltipText;
	}

	@Override
	public String getCommand() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < commands.size(); i++) {
			TerminalCommand command = commands.get(i);
			sb.append(command.getCommand());
			if (commands.size() != i + 1) {
				sb.append(" && ");
			}
		}
		return sb.toString();
	}

	public List<TerminalCommand> getCommands() {
		return commands;
	}

	public List<TerminalCommand> getOriginalCommands() {
		return sshCommandOriginal;
	}

	@Override
	public String getCommandRaw() {
		return "";
	}

	@Override
	public int getPriority() {
		return position;
	}

	public List<String> getCommandsRaw() {
		List<String> commandsStringList = new LinkedList<>();
		for (TerminalCommand c : commands) {
			String comstr = c.getCommandRaw();
			if (!(comstr == null || comstr.isBlank())) {
				commandsStringList.add(c.getCommandRaw());
			}
		}

		return commandsStringList;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public FGeneralDialog getDialog() {
		return null;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String toString() {
		StringBuilder com = new StringBuilder("{");
		com.append(TerminalCommandFactory.COMMAND_MAP_ID).append(":").append(getId()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_PARENT_MENU_TEXT).append(":").append(getParentMenuText())
				.append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_MENU_TEXT).append(":").append(getMenuText()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_TOOLTIP_TEXT).append(":").append(getToolTipText()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_POSITION).append(":").append(getPriority()).append(", ");
		com.append(TerminalCommandFactory.COMMAND_MAP_COMMANDS).append(":").append("[");
		for (int i = 0; i < getCommandsRaw().size(); i++) {
			String c = getCommandsRaw().get(i);
			if (i == getCommandsRaw().size() - 1) {
				com.append(c);
			} else {
				com.append(c).append(",");
			}
		}
		com.append("]");
		com.append("}");
		return com.toString();
	}

	public String commandlistToString() {
		StringBuilder commandString = new StringBuilder("[");
		for (int i = 0; i < getCommands().size(); i++) {
			String c = ((EmptyCommand) getCommands().get(i)).commandToString();
			if (i == getCommands().size() - 1) {
				commandString.append(c);
			} else {
				commandString.append(c).append(",");
			}
		}
		commandString.append("]");
		return commandString.toString();
	}

	@Override
	public int compareTo(TerminalMultiCommand compareCom) {
		int dif = this.position - compareCom.getPriority();
		if (dif == 0) {
			return this.menuText.compareTo(compareCom.getMenuText());
		}
		return dif;
	}

	public TerminalMultiCommand update(TerminalMultiCommand com) {
		if (this.id.equals(com.getId())) {
			Logging.debug(this, "update this (" + this.toString() + ") with (" + com.toString() + ")");
			setCommands(com.getCommandsRaw());
			setMenuText(com.getMenuText());
			setParentMenuText(com.getParentMenuText());
			setTooltipText(com.getToolTipText());
			setPriority(com.getPriority());
		}
		Logging.info(this, "updated command: " + this.toString());
		return this;
	}

	@Override
	public boolean equals(Object o) {
		TerminalMultiCommand com = null;
		if (o instanceof TerminalMultiCommand) {
			com = (TerminalMultiCommand) o;
		} else {
			Logging.debug(this, "equals object is not instance of SSHCommandTemplate");
			return false;
		}

		if (!areBothNull(this.getId(), com.getId()) || !this.getId().trim().equals(com.getId().trim())) {
			Logging.debug(this, "equals different id's " + this.getId() + " != " + com.getId() + "");
			return false;
		}
		if (!areBothNull(this.getMenuText(), com.getMenuText())
				|| !this.getMenuText().trim().equals(com.getMenuText().trim())) {
			Logging.debug(this, "equals different menuText's " + this.getMenuText() + " != " + com.getMenuText() + "");
			return false;
		}
		if (!areBothNull(this.getParentMenuText(), com.getParentMenuText())
				|| !this.getParentMenuText().trim().equals(com.getParentMenuText().trim())) {
			Logging.debug(this, "equals different parentMenuText's " + this.getParentMenuText() + " != "
					+ com.getParentMenuText() + "");
			return false;
		}
		if (!areBothNull(this.getToolTipText(), com.getToolTipText())
				|| !this.getToolTipText().trim().equals(com.getToolTipText().trim())) {
			Logging.debug(this,
					"equals different toolTipText's " + this.getToolTipText() + " != " + com.getToolTipText() + "");
			return false;
		}
		if (this.getPriority() != com.getPriority()) {
			Logging.debug(this, "equals different priorities " + this.getPriority() + " != " + com.getPriority() + "");
			return false;
		}
		if (this.getCommandsRaw().size() != com.getCommandsRaw().size()) {
			Logging.debug(this, "equals different commandlist length " + this.getCommandsRaw().size() + " != "
					+ com.getCommandsRaw().size() + "");
			return false;
		}

		for (int i = 0; i < this.getCommandsRaw().size(); i++) {
			if (!this.getCommandsRaw().get(i).equals(com.getCommandsRaw().get(i))) {
				Logging.debug(this,
						"equals different commands " + this.getCommandsRaw() + " != " + com.getCommandsRaw() + "");
				return false;
			}
		}
		Logging.debug(this, "equals commands are equal");
		return true;
	}

	private static boolean areBothNull(String a, String b) {
		return a == null && b == null;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
