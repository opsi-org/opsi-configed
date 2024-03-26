/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

import java.util.LinkedList;
import java.util.List;

import de.uib.utilities.logging.Logging;

public class TerminalMultiCommandTemplate implements TerminalMultiCommand, Comparable<TerminalMultiCommandTemplate> {
	private String id;
	private String menuText;
	private List<TerminalSingleCommand> commands = new LinkedList<>();
	private List<TerminalSingleCommand> sshCommandOriginal = new LinkedList<>();
	private String parentMenuText;
	private String tooltipText = "";
	private int priority;

	private String mainName = "";

	private boolean firstInitCommands = true;

	public TerminalMultiCommandTemplate() {
		priority = TerminalCommandFactory.DEFAULT_POSITION;
	}

	public TerminalMultiCommandTemplate(String id, List<String> c, String mt, String pmt, String ttt, int p) {
		priority = TerminalCommandFactory.DEFAULT_POSITION;

		initValues(id, c, mt, pmt, ttt, p);
	}

	public TerminalMultiCommandTemplate(TerminalCommandMetadata orig, List<String> commandlist) {
		this(orig.getId(), commandlist, orig.getMenuText(), orig.getParentMenuText(), orig.getToolTipText(),
				orig.getPriority());
	}

	public TerminalMultiCommandTemplate(TerminalMultiCommandTemplate orig) {
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

	@Override
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
				TerminalSingleCommand sshc = new TerminalSingleCommandTemplate(getId(), c, getMenuText());
				commands.add(sshc);
				if (firstInitCommands) {
					sshCommandOriginal.add(sshc);
				}
			}
			firstInitCommands = false;
		}
	}

	public void addCommand(TerminalSingleCommand sshc) {
		commands.add(sshc);
		sshCommandOriginal.add(sshc);
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
		priority = p;
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
	public List<TerminalSingleCommand> getCommands() {
		return commands;
	}

	public List<TerminalSingleCommand> getOriginalCommands() {
		return sshCommandOriginal;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public List<String> getCommandsRaw() {
		List<String> commandsStringList = new LinkedList<>();
		for (TerminalSingleCommand c : commands) {
			String comstr = c.getCommandRaw();
			if (!(comstr == null || comstr.isBlank())) {
				commandsStringList.add(c.getCommandRaw());
			}
		}

		return commandsStringList;
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
			String c = ((TerminalSingleCommandTemplate) getCommands().get(i)).commandToString();
			if (i == getCommands().size() - 1) {
				commandString.append(c);
			} else {
				commandString.append(c).append(",");
			}
		}
		commandString.append("]");
		return commandString.toString();
	}

	public TerminalMultiCommandTemplate update(TerminalMultiCommandTemplate com) {
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
	public int compareTo(TerminalMultiCommandTemplate compareCom) {
		int dif = this.getPriority() - compareCom.getPriority();
		if (dif == 0) {
			return this.getMenuText().compareTo(compareCom.getMenuText());
		}
		return dif;
	}

	@Override
	public boolean equals(Object o) {
		TerminalMultiCommandTemplate com = null;
		if (o instanceof TerminalMultiCommandTemplate) {
			com = (TerminalMultiCommandTemplate) o;
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
}
