package de.uib.opsicommand.sshcommand;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.gui.FGeneralDialog;
import de.uib.utilities.logging.Logging;

/**
 * This class represent a ssh-command
 **/
public class SSHCommandTemplate implements SSHCommand, Comparable<SSHCommandTemplate>, SSHMultiCommand {
	/** boolean needParameter = false **/
	private boolean needParameter = false;
	/** boolean isMultiCommand = true **/
	private boolean isMultiCommand = true;
	/** String unique command id **/
	private String id;
	/** String unique menu text **/
	private String menuText;
	/** LinkedList<SSHCommand> ssh_command **/
	private LinkedList<SSHCommand> sshCommand = new LinkedList<>();
	private LinkedList<SSHCommand> sshCommandOriginal = new LinkedList<>();
	/** boolean needSudo state **/
	private boolean needSudo = false;
	/** String parent menu text **/
	private String parentMenuText = null;
	/** String tooltip text **/
	private String tooltipText = "";
	/** integer position **/
	private int position;

	private static final String CONFIDENTIAL_INFORMATION = null;

	/**
	 * Creates an empty SSHCommand_Template instance
	 * 
	 * @return SSHCommand_Template instance
	 */
	public SSHCommandTemplate() {
		position = SSHCommandFactory.POSITION_DEFAULT;
	}

	/**
	 * Creates an SSHCommand_Template instance with given parameter
	 * 
	 * @param id  : String
	 * @param c   (commands): LinkedList<String>
	 * @param mt  (menu text): String
	 * @param ns  (needSudo): boolean
	 * @param pmt (parent menu text) : String
	 * @param ttt (tooltip text): String
	 * @param p   (position): int
	 * @return SSHCommand_Template instance
	 */
	public SSHCommandTemplate(String id, List<String> c, String mt, boolean ns, String pmt, String ttt, int p) {
		position = SSHCommandFactory.POSITION_DEFAULT;
		setId(id);
		setMenuText(mt);
		setNeedSudo(ns);
		setParentMenuText(pmt);
		setTooltipText(ttt);
		setPriority(p);
		setCommands(c);
		Logging.debug(this, "SSHCommand_Template this " + this.toString());
		Logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}

	public SSHCommandTemplate(SSHCommand orig, List<String> commandlist) {
		this(orig.getId(), commandlist, orig.getMenuText(), orig.needSudo(), orig.getParentMenuText(),
				orig.getToolTipText(), orig.getPriority());
		Logging.debug(this, "SSHCommand_Template this " + this.toString());
		Logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}

	public SSHCommandTemplate(SSHCommandTemplate orig) {
		this(orig.getId(), orig.getCommandsRaw(), orig.getMenuText(), orig.needSudo(), orig.getParentMenuText(),
				orig.getToolTipText(), orig.getPriority());
		Logging.debug(this, "SSHCommand_Template this " + this.toString());
		Logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String get_ERROR_TEXT() {
		return "ERROR";
	}

	private String mainName = "";

	@Override
	public String getMainName() {
		return mainName;
	}

	public void setMainName(String n) {
		mainName = n;
	}

	/**
	 * Sets the Id
	 * 
	 * @param i (id): String
	 **/
	public void setId(String i) {
		id = i;
	}

	private boolean firstInitCommands = true;

	/**
	 * Sets the given commandlist
	 * 
	 * @param c_list: LinkedList<String>
	 **/
	public void setCommands(List<String> c_list) {
		if (c_list != null) {
			for (String c : c_list) {

				SSHCommand sshc = new EmptyCommand(getId(), c, getMenuText(), needSudo());
				sshCommand.add(sshc);
				if (firstInitCommands)
					sshCommandOriginal.add(sshc);
			}
			firstInitCommands = false;
		}
	}

	/**
	 * Sets the given command
	 * 
	 * @param c (command): String
	 **/
	@Override
	public void setCommand(String c) {
	}

	/**
	 * Add given SSHCommand to commandlist
	 * 
	 * @param sshc: SSHCommand
	 **/
	public void addCommand(SSHCommand sshc) {
		sshCommand.add(sshc);
		sshCommandOriginal.add(sshc);
	}

	@Override
	public String getSecureInfoInCommand() {
		return CONFIDENTIAL_INFORMATION;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().equals(""))) {
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		} else
			return getCommand();
	}

	/**
	 * Sets the menu text
	 * 
	 * @param mt (menu text): String
	 **/
	public void setMenuText(String mt) {
		menuText = mt;
	}

	/**
	 * Sets the parent menu text
	 * 
	 * @param pmt (parent menu text): String
	 **/
	public void setParentMenuText(String pmt) {
		parentMenuText = pmt;
	}

	/**
	 * Sets the tooltip text
	 * 
	 * @param ttt (tooltip text): String
	 **/
	public void setTooltipText(String ttt) {
		tooltipText = ttt;
	}

	/**
	 * Sets the need sudo state
	 * 
	 * @param ns (needSudo): boolean
	 **/
	public void setNeedSudo(boolean ns) {
		needSudo = ns;
	}

	/**
	 * Sets the position
	 * 
	 * @param p (position): int
	 **/
	public void setPriority(int p) {
		position = p;
	}

	/**
	 * Get the command id
	 * 
	 * @return id
	 **/
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Get the trimmed command menutext
	 * 
	 * @return menuText
	 **/
	@Override
	public String getMenuText() {
		if (menuText != null && menuText.length() > 0)
			return menuText.trim();

		return menuText;
	}

	/**
	 * Get the command parent menutext
	 * 
	 * @return parentMenuText
	 **/
	@Override
	public String getParentMenuText() {
		return parentMenuText;
	}

	/**
	 * Get the command tooltip text
	 * 
	 * @return tooltip text
	 **/
	@Override
	public String getToolTipText() {
		return tooltipText;
	}

	/**
	 * Get the command from sshcommand
	 * 
	 * @return command
	 **/
	@Override
	public String getCommand() {
		return "";
	}

	/**
	 * Get the all commands in sshcommand
	 * 
	 * @return List of SSHCommand
	 **/
	@Override
	public LinkedList<SSHCommand> getCommands() {
		return sshCommand;
	}

	public LinkedList<SSHCommand> getOriginalCommands() {
		return sshCommandOriginal;
	}

	/**
	 * Get the command without parameter from SSHCommand
	 * 
	 * @return command
	 **/
	@Override
	public String getCommandRaw() {
		return "";
	}

	/**
	 * @return True if the commands needs sudo
	 **/
	@Override
	public boolean needSudo() {
		return needSudo;
	}

	/**
	 * @return the position
	 */
	@Override
	public int getPriority() {
		return position;
	}

	/**
	 * Format the commands(List<SSHCommands>) to LinkedList<String>
	 * 
	 * @return LinkedList<String> with the commands
	 **/
	@Override
	public LinkedList<String> getCommandsRaw() {
		LinkedList<String> commandsStringList = new LinkedList<>();
		for (SSHCommand c : sshCommand) {
			String comstr = c.getCommandRaw();
			if (!((comstr == null) || (comstr.trim().equals(""))))
				commandsStringList.add(c.getCommandRaw());
		}

		return commandsStringList;
	}

	/**
	 * @return True if the commands needs Parameter
	 **/
	@Override
	public boolean needParameter() {
		return needParameter;
	}

	/**
	 * @return null (SSHCommand_Template does not have a parameter dialog)
	 **/
	@Override
	public FGeneralDialog getDialog() {
		return null;
	}

	/**
	 * @return True
	 */
	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	/**
	 * @return a string representation of this command
	 */
	@Override
	public String toString() {
		StringBuilder com = new StringBuilder("{");
		com.append(SSHCommandFactory.COMMAND_MAP_ID).append(":").append(getId()).append(",");
		com.append(SSHCommandFactory.COMMAND_MAP_PARENT_MENU_TEXT).append(":").append(getParentMenuText()).append(",");
		com.append(SSHCommandFactory.COMMAND_MAP_MENU_TEXT).append(":").append(getMenuText()).append(",");
		com.append(SSHCommandFactory.COMMAND_MAP_TOOLTIP_TEXT).append(":").append(getToolTipText()).append(",");
		com.append(SSHCommandFactory.COMMAND_MAP_NEED_SUDO).append(":").append(needSudo()).append(",");
		com.append(SSHCommandFactory.COMMAND_MAP_POSITION).append(":").append(getPriority()).append(", ");
		com.append(SSHCommandFactory.COMMAND_MAP_COMMANDS).append(":").append("[");
		for (int i = 0; i < getCommandsRaw().size(); i++) {
			String c = getCommandsRaw().get(i);
			if (i == getCommandsRaw().size() - 1)
				com.append(c);
			else
				com.append(c).append(",");
		}
		com.append("]");
		com.append("}");
		return com.toString();
	}

	public String commandlistToString() {
		StringBuilder commandString = new StringBuilder("[");
		for (int i = 0; i < getCommands().size(); i++) {
			String c = ((EmptyCommand) getCommands().get(i)).commandToString();
			if (i == getCommands().size() - 1)
				commandString.append(c);
			else
				commandString.append(c).append(",");
		}
		commandString.append("]");
		return commandString.toString();
	}

	/**
	 * Compares the position of SSHCommand_Templates. If it is equal compare by
	 * menuText
	 * 
	 * @param compareCom Compares the compareCom to this command
	 * @return difference
	 */
	@Override
	public int compareTo(SSHCommandTemplate compareCom) {
		int dif = this.position - compareCom.getPriority();
		if (dif == 0)
			return this.menuText.compareTo(compareCom.getMenuText());
		return dif;
	}

	/**
	 * Update all fields of this command to the fields of given command
	 * 
	 * @param SSHCommandTemplate com
	 * @return the updated command (this)
	 */
	public SSHCommandTemplate update(SSHCommandTemplate com) {
		if (this.id.equals(com.getId())) {
			Logging.debug(this, "update this (" + this.toString() + ") with (" + com.toString() + ")");
			setCommands(com.getCommandsRaw());
			setMenuText(com.getMenuText());
			setNeedSudo(com.needSudo());
			setParentMenuText(com.getParentMenuText());
			setTooltipText(com.getToolTipText());
			setPriority(com.getPriority());
		}
		Logging.info(this, "updated command: " + this.toString());
		return this;
	}

	public boolean equals(SSHCommandTemplate com) {
		if (!this.getId().trim().equals(com.getId().trim())) {
			Logging.debug(this, "equals different id's " + this.getId() + " != " + com.getId() + "");
			return false;
		}
		if (!this.getMenuText().trim().equals(com.getMenuText().trim())) {
			Logging.debug(this, "equals different menuText's " + this.getMenuText() + " != " + com.getMenuText() + "");
			return false;
		}
		if (!this.getParentMenuText().trim().equals(com.getParentMenuText().trim())) {
			Logging.debug(this, "equals different parentMenuText's " + this.getParentMenuText() + " != "
					+ com.getParentMenuText() + "");
			return false;
		}
		if (!this.getToolTipText().trim().equals(com.getToolTipText().trim())) {
			Logging.debug(this,
					"equals different toolTipText's " + this.getToolTipText() + " != " + com.getToolTipText() + "");
			return false;
		}
		if (this.getPriority() != com.getPriority()) {
			Logging.debug(this, "equals different priorities " + this.getPriority() + " != " + com.getPriority() + "");
			return false;
		}
		if (this.needSudo() != com.needSudo()) {
			Logging.debug(this, "equals different needSudo " + this.needSudo() + " != " + com.needSudo() + "");
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

	/**
	 * @return null
	 */
	@Override
	public List<String> getParameterList() {
		return null;
	}
}
