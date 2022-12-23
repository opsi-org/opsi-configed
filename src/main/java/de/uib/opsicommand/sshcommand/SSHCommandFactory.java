package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.MainFrame;
import de.uib.utilities.logging.logging;

/**
 * This Class handles SSHCommands.
 **/
public class SSHCommandFactory {
	/** final string commands for linux terminal **/
	public final String str_replacement_dir = "*.dir.*";
	// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
	public final String str_command_getLinuxCommands = "COMMANDS=`echo -n $PATH | xargs -d : -I {} find {} -maxdepth 1 -executable -type f -printf '%P\\n'` ; ALIASES=`alias | cut -d '=' -f 1`; echo \"$COMMANDS\"$'\\n'\"$ALIASES\" | sort -u ";
	public final String str_command_getDirectories = "ls --color=never -d *.dir.*/*/";
	public final String str_command_getOpsiFiles = "ls --color=never *.dir.*/*.opsi";
	public final String str_command_getVersions = "grep version: *.dir.* --max-count=2  ";
	public final String str_command_catDir = "cat *.dir.*OPSI/control | grep \"id: \"";
	public final String str_command_fileexists = "[ -f .filename. ] &&  rm .filename. && echo \"File .filename. removed\" || echo \"File did not exist\"";
	public final String str_command_fileexists_notremove = "[ -d .filename. ] && echo \"File exists\" || echo \"File not exist\"";
	// public final String str_command_filezsyncExists = "[ -f *.filename.*.zsync ]
	// && rm *.filename.*.zsync && echo \"File *.filename.*.zsync removed\" || echo
	// \"File *.filename.*.zsync did not exist\"";
	// public final String str_command_filemd5Exists = "[ -f *.filename.*.md5 ] &&
	// rm *.filename.*.md5 && echo \"File *.filename.*.md5 removed\" || echo \"File
	// *.filename.*.md5 did not exist\"";
	public final String str_replacement_filename = ".filename.";
	public final String str_file_exists = "File exists";
	public final String str_file_not_exists = "File not exists";

	public final String opsipathVarRepository = "/var/lib/opsi/repository/";
	public final String opsipathVarDepot = "/var/lib/opsi/depot/";
	// public final String str_command_comparemd5 = " if [ -z $((cat
	// *.product.*.md5" + ") | grep $(md5sum *.product.* | head -n1 | cut -d \" \"
	
	// public final String str_replacement_product="*.product.*";
	// public final String str_replacement_equal= "*.md5equal.*";
	// public final String str_replacement_notequal= "*.md5notequal.*";

	/** ConfigedMain instance **/
	private ConfigedMain main;
	private MainFrame mainFrame;
	/** SSHCommandFactory instance **/
	private static SSHCommandFactory instance;
	/**
	 * List<Map<String,Object>> list elements are commands with key value pairs
	 **/
	private List<Map<java.lang.String, java.lang.Object>> commandlist;
	/** List<SSHCommand_Template> list elements are sshcommands **/
	private List<SSHCommand_Template> sshcommand_list;
	/** list of known menus **/
	private List<String> list_knownMenus;
	/** list of known parent menus **/
	private List<String> list_knownParents;

	/** static String for parent null ("Server-Konsole") **/
	public static final String parentNull = configed.getResourceValue("MainFrame.jMenuServer");
	/**
	 * static String defined as language independent parent for own commands
	 **/
	public static final String parentdefaultForOwnCommands = "...";
	/** static String for specific parent ("opsi") **/
	public static final String parentOpsi = configed.getResourceValue("MainFrame.jMenuOpsi");
	/** static String for new command ("<Neuer Befehl>") **/
	public static final String menuNew = configed.getResourceValue("SSHConnection.CommandControl.menuText_newCommand");
	/** default position is 0 **/
	public final int position_default = 0;

	public static int successfulConnectObservedCount = 0;

	/** default parameter replace id beginns with <<< **/
	// public static String replacement_default_1 = "<<<";
	/** default parameter replace id ends with >>> **/
	// public static String replacement_default_2 = ">>>";

	/** setting ssh_colored_output per default true **/
	public static boolean ssh_colored_output = true;
	/** setting ssh_always_exec_in_background per default false **/
	public static boolean ssh_always_exec_in_background = false;
	/** all static commands which need run-time parameter **/
	public static List<de.uib.opsicommand.sshcommand.SSHCommand> ssh_commands_param = new LinkedList<>();

	/** static final name of field "id" */
	public final String command_map_id = "id";
	/** static final name of field "menuText" */
	public final String command_map_menuText = "menuText";
	/** static final name of field "parentMenuText" */
	public final String command_map_parentMenuText = "parentMenuText";
	/** static final name of field "tooltipText" */
	public final String command_map_tooltipText = "tooltipText";
	/** static final name of field "position" */
	public final String command_map_position = "position";
	/** static final name of field "needSudo" */
	public final String command_map_needSudo = "needSudo";
	/** static final name of field "commands" */
	public final String command_map_commands = "commands";

	SSHConnectExec connection = null;
	public static final String CONNECTED = configed.getResourceValue("SSHConnection.connected");
	public static final String CONNECTION_NOT_ALLOWED = configed
			.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED");
	public static final String UNKNOWN = configed.getResourceValue("SSHConnection.unknown");
	public static final String NOT_CONNECTED = configed.getResourceValue("SSHConnection.not_connected");

	// SSHCommandFactory.getInstance().sudo_text
	public static String sudo_failed_text = configed.getResourceValue("SSHConnection.sudoFailedText");
	public static String sudo_text = "sudo -S -p \"" + sudo_failed_text + "\" ";

	public final String sshusr = "<<!sshuser!>>";
	public final String sshhst = "<<!sshhost!>>";

	public final String confidential = "***confidential***";
	ArrayList<String> createdProducts = new ArrayList<>();

	SSHCommandParameterMethods pmethodHandler = null;
	SSHConnectionInfo connectionInfo = null;

	/**
	 * Factory Instance for SSH Command
	 * 
	 * @param main {@link de.uib.configed.ConfigedMain} class
	 **/
	private SSHCommandFactory(ConfigedMain main) {
		this.main = main;
		logging.info(this, "SSHComandFactory new instance");
		instance = this;
		addAditionalParamCommands();
		connection = new SSHConnectExec(this.main);
		connectionInfo = SSHConnectionInfo.getInstance();
		pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @param main {@link de.uib.configed.ConfigedMain} class
	 * @return SSHCommandFactory instance
	 **/
	public static SSHCommandFactory getInstance(ConfigedMain m) {
		if (instance != null)
			return instance;
		else
			return new SSHCommandFactory(m);
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @return SSHCommandFactory instance
	 **/
	public static SSHCommandFactory getInstance() {
		if (instance != null)
			return instance;
		else
			return new SSHCommandFactory(null);
	}

	protected Set<String> allowedHosts = new HashSet<>();

	public void setAllowedHosts(Collection<String> allowed) {
		allowedHosts.addAll(allowed);
	}

	public Set<String> getAllowedHosts() {
		return allowedHosts;
	}

	private void addAditionalParamCommands() {
		if (!(main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<'
				|| main.getOpsiVersion().compareTo("4.1") < 0))
			ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandPackageUpdater());
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall());
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall());
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsimakeproductfile());
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandWget());
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandModulesUpload());
		if (!(main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<'
				|| main.getOpsiVersion().compareTo("4.1") < 0))
			ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandRepositoryUpload());

		// Funktioniert nicht wie gewünscht. Optionaler Parameter "<<<....>>>" wird
		// nicht abgefragt.
		// ssh_commands_param.add(new
		
		// SSHCommand csetrights = new
		
		// LinkedList<String> coms = new LinkedList<>()
		// {{
		
		
		// ssh_commands_param.add(new SSHCommand_Template(csetrights, new
		
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiSetRights());
		// ssh_commands_param.add(new SSHCommand_Template(opsisetrights, coms,
		// configed.getResourceValue("SSHConnection.command.opsisetrights"), true, null,
		// configed.getResourceValue("SSHConnection.command.opsisetrights.tooltip"),
		
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandDeployClientAgent());

	}

	public ArrayList<String> getProductHistory() {
		return createdProducts;
	}

	public void addProductHistory(String prod) {
		createdProducts.add(prod);
	}

	public SSHCommandParameterMethods getParameterHandler() {
		if (pmethodHandler != null)
			return pmethodHandler;
		else {
			pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
			return pmethodHandler;
		}
	}

	public void setMainFrame(MainFrame mf) {
		if (mf != null)
			mainFrame = mf;
	}

	/**
	 * Testing the confd-method 'SSHCommand_getObjects'
	 * 
	 * @return True if method exists
	 **/
	public boolean checkSSHCommandMethod() {
		return main.getPersistenceController().checkSSHCommandMethod("SSHCommand_getObjects");
	}

	/**
	 * Sets the commandlist to null
	 **/
	public void retrieveSSHCommandListRequestRefresh() {
		logging.info(this, "retrieveSSHCommandListRequestRefresh commandlist null");
		commandlist = null;
	}

	/**
	 * Builds a SSHCommand_Template uses the given parameter
	 * 
	 * @param id  : String
	 * @param pmt (parent menu text) : String
	 * @param mt  (menu text): String
	 * @param ttt (tooltip text): String
	 * @param p   (position): int
	 * @param ns  (needSudo): boolean
	 * @param c   (commands): LinkedList<String>
	 * @return new SSHCommand_Template
	 **/
	public SSHCommand_Template buildSSHCommand(String id, String pmt, String mt, String ttt, int p, boolean ns,
			LinkedList<String> c) {
		SSHCommand_Template com = new SSHCommand_Template(id, c, // Achtung Reihenfolge der Elemente in Arrays c könnte sich ändern !" toList =
				// ArrayList! JsonArray muss nicht sortiert sein!"
				mt, ns, pmt, ttt, p);
		return com;
	}

	/**
	 * retrieve commandlist from persistencecontroller (if commandlist is null)
	 * and build sshcommandlist
	 * 
	 * @return List<SSHCommand_Template>
	 **/
	public List<SSHCommand_Template> retrieveSSHCommandList() {
		logging.info(this, "retrieveSSHCommandList ");
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();

		sshcommand_list = new ArrayList<>();
		list_knownMenus = new ArrayList<>();
		list_knownParents = new ArrayList<>();

		if (!commandlist.isEmpty())
			list_knownParents.add(parentdefaultForOwnCommands);

		
		

		list_knownMenus.add(parentdefaultForOwnCommands);
		

		for (Map<java.lang.String, java.lang.Object> map : commandlist) {
			SSHCommand_Template com = buildSSHCommand(((String) map.get(command_map_id)),
					((String) map.get(command_map_parentMenuText)), ((String) map.get(command_map_menuText)),
					((String) map.get(command_map_tooltipText)), ((int) map.get(command_map_position)),
					((boolean) map.get(command_map_needSudo)), null);
			if (map.get(command_map_commands) != null) {
				// Achtung Reihenfolge könnte sich ändern !" toList = ArrayList! JsonArray muss
				// nicht sortiert sein!"
				LinkedList com_commands = new LinkedList<>(((JSONArray) map.get(command_map_commands)).toList());
				com_commands.add("echo ... ");
				com_commands.add("echo READY");
				com.setCommands(com_commands);
			}
			list_knownMenus.add(com.getMenuText());

			String parent = com.getParentMenuText();

			logging.info(this, "parent menu text " + parent);
			
			if (parent == null || parent.equalsIgnoreCase("null") || parent == parentdefaultForOwnCommands)
				parent = parentdefaultForOwnCommands;
			if (!list_knownParents.contains(parent))
				list_knownParents.add(parent);

			logging.info(this, "parent menu text changed  " + parent);

			logging.info(this, "list_knownParents " + list_knownParents);

			

			sshcommand_list.add(com);
		}
		return sshcommand_list;
	}

	/**
	 * Sort all menu names alphabeticly
	 * 
	 * @return List<String> sorted list_knownMenus
	 **/
	public List<String> getSSHCommandMenuNames() {
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		Collections.sort(list_knownMenus, (String s1, String s2) -> s1.compareToIgnoreCase(s2));
		return list_knownMenus;
	}

	/**
	 * Sort all parent menus alphabeticly
	 * 
	 * @return List<String> sorted list_knownParents
	 **/
	public List<String> getSSHCommandMenuParents() {
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		Collections.sort(list_knownParents, (String s1, String s2) -> s1.compareToIgnoreCase(s2));
		return list_knownParents;
	}

	/**
	 * Sorts all SSHCommands by position, after that sorts by there parent menus
	 * (keep position order in parent menus).
	 * 
	 * @return java.util.LinkedHashMap<String,List<SSHCommand_Template>>
	 *         sortedComs
	 **/
	public Map<String, List<SSHCommand_Template>> getSSHCommandMapSortedByParent() {
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();

		logging.info(this, "getSSHCommandMapSortedByParent sorting commands ");
		Collections.sort(sshcommand_list);

		java.util.LinkedHashMap<String, List<SSHCommand_Template>> sortedComs = new LinkedHashMap<>();
		
		sortedComs.put(parentdefaultForOwnCommands, new LinkedList<>());
		sortedComs.put(parentOpsi, new LinkedList<>());

		for (SSHCommand_Template com : sshcommand_list) {
			String parent = com.getParentMenuText();
			if ((parent == null) || (parent.trim().equals(""))) {
				parent = parentdefaultForOwnCommands;
				
			}
			List parentList = new LinkedList<>();
			if (sortedComs.containsKey(parent))
				parentList = sortedComs.get(parent);
			else
				sortedComs.put(parent, parentList);

			if (!(parentList.contains(com)))
				parentList.add(com);
		}
		return sortedComs;
	}

	/**
	 * Get list of SSHCommands which need run-time parameter
	 * 
	 * @return List<SSHCommand> ssh_commands_param
	 **/
	public List<SSHCommand> getSSHCommandParameterList() {
		logging.info(this, "getSSHCommandParameterList ");
		return ssh_commands_param;
	}

	/**
	 * Search sshcommand with given menu name
	 * 
	 * @param menu
	 * @return SSHCommand_Template
	 **/
	public SSHCommand_Template getSSHCommandByMenu(String menu) {
		if (sshcommand_list == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		for (SSHCommand_Template c : sshcommand_list)
			if (c.getMenuText().equals(menu))
				return c;
		return null;
	}

	/**
	 * Build an Map of key-value-pairs from a SSHCommand_Template
	 * 
	 * @param SSHCommand_Template
	 * @return Map<String,Object> command
	 **/
	private Map<String, Object> buildCommandMap(SSHCommand_Template c) {
		Map<String, Object> com = new HashMap<>();
		// com.put("id", c.getId());
		com.put(command_map_menuText, c.getMenuText());
		com.put(command_map_parentMenuText, c.getParentMenuText());
		com.put(command_map_tooltipText, c.getToolTipText());
		com.put(command_map_position, c.getPriority());
		com.put(command_map_needSudo, c.needSudo());
		return com;
	}

	/**
	 * Create or update an command (update local lists)
	 * 
	 * @param SSHCommand_Template command
	 **/
	public boolean saveSSHCommand(SSHCommand_Template command) {
		logging.info(this, "saveSSHCommand command " + command.toString());
		List<Object> jsonObjects = new ArrayList<>();
		try {
			JSONObject jsComMap = new JSONObject(buildCommandMap(command));
			JSONArray jsComArrCom = new JSONArray(((SSHMultiCommand) command).getCommandsRaw());
			jsComMap.put(command_map_commands, jsComArrCom);
			jsonObjects.add(jsComMap);
		} catch (Exception e) {
			logging.warning(this, "saveSSHCommand, exception occurred", e);
		}

		if (list_knownMenus.contains(command.getMenuText())) {
			logging.info(this, "saveSSHCommand sshcommand_list.contains(command) true");
			if (main.getPersistenceController().updateSSHCommand(jsonObjects)) 
			{
				((SSHCommand_Template) sshcommand_list
						.get(sshcommand_list.indexOf(getSSHCommandByMenu(command.getMenuText())))).update(command);
				return true;
			}
		} else {
			logging.info(this, "saveSSHCommand sshcommand_list.contains(command) false");
			if (main.getPersistenceController().createSSHCommand(jsonObjects)) {
				sshcommand_list.add(command);
				list_knownMenus.add(command.getMenuText());
				return true;
			}
		}
		return false;
	}

	public boolean isSSHCommandEqualSavedCommand(SSHCommand_Template command) {
		if (list_knownMenus.contains(command.getMenuText())) {
			logging.info(this, "isSSHCommandEqualSavedCommand compare command " + command.toString());
			if (sshcommand_list == null) {
				logging.info(this, "isSSHCommandEqualSavedCommand  command_list == null ");
				return false;
			}
			if (sshcommand_list.isEmpty()) {
				logging.info(this, "isSSHCommandEqualSavedCommand  command_list has no elements ");
				return false;
			}

			if (getSSHCommandByMenu(command.getMenuText()) == null) {
				logging.info(this,
						" isSSHCommandEqualSavedCommand getSSHCommandByMenu( command.getMenuText() ) is null ");
				return false;
			}

			logging.info(this, "isSSHCommandEqualSavedCommand  command_list " + (sshcommand_list));

			logging.info(this, "isSSHCommandEqualSavedCommand with found " + ((SSHCommand_Template) sshcommand_list
					.get(sshcommand_list.indexOf(getSSHCommandByMenu(command.getMenuText())))));
			logging.info(this, "isSSHCommandEqualSavedCommand equals " + ((SSHCommand_Template) sshcommand_list
					.get(sshcommand_list.indexOf(getSSHCommandByMenu(command.getMenuText())))).equals(command));

			if (((SSHCommand_Template) sshcommand_list
					.get(sshcommand_list.indexOf(getSSHCommandByMenu(command.getMenuText())))).equals(command)) {
				return true;
			} else
				return false;
		}
		return false;
	}

	/**
	 * Delete the command with given menu text
	 * 
	 * @param String menu
	 **/
	public void deleteSSHCommandByMenu(String menu) {
		logging.info(this, "deleteSSHCommand menu " + menu);
		// return
		List<String> jsonObjects = new ArrayList<>();
		jsonObjects.add(menu);
		if (main.getPersistenceController().deleteSSHCommand(jsonObjects)) {
			sshcommand_list.remove(getSSHCommandByMenu(menu));
			list_knownMenus.remove(menu);
		}
	}

	/**
	 * Reload configed menu server-konsole
	 */
	public void reloadServerMenu() {
		new Thread() {
			@Override
			public void run() {
				main.reloadServerMenu();
			}
		}.start();
	}

	String connectionState = NOT_CONNECTED;

	public SSHConnect getConnection() {
		return connection;
	}

	public String getConnectionState() {
		return connectionState;
	}

	public void unsetConnection() {
		com.jcraft.jsch.Session session = SSHCommandFactory.getInstance().getConnection().getSession();
		if (session != null) {
			session.disconnect();
		}
		connectionState = NOT_CONNECTED;
		updateConnectionInfo(connectionState);
	}

	public void testConnection(String user, String host) {
		SSHCommand command = new Empty_Command(Empty_Command.TESTCOMMAND, Empty_Command.TESTCOMMAND, "", false);
		connectionState = UNKNOWN;
		try {
			if (connection.connect(command)) {
				String result = connection.exec(command, false);
				logging.info(this, "connection.exec produces " + result);
				if (result == null || result.trim().equals("")) {
					logging.info(this, "testConnection not allowed");
					connectionState = CONNECTION_NOT_ALLOWED;
					logging.warning(this, "cannot connect to " + user + "@" + host);
				} else {
					logging.info(this, "testConnection connected");
					connectionState = CONNECTED;
				}
			} else {
				logging.info(this, "testConnection not connected");
				connectionState = NOT_CONNECTED;
				logging.warning(this, "cannot connect to " + user + "@" + host);
			}

		} catch (Exception e) {
			logging.info(this, "testConnection not connected");
			connectionState = NOT_CONNECTED;
			logging.warning(this, "cannot connect to " + user + "@" + host);
		}
		updateConnectionInfo(connectionState);
		logging.info(this, "testConnection connection state " + connectionState);
	}

	public void updateConnectionInfo(String status) {
		logging.info(this, "mainFrame " + mainFrame);
		logging.info(this, "Globals.mainFrame " + Globals.mainFrame);

		logging.info(this, "status " + status);
		if (mainFrame == null)
			((MainFrame) Globals.mainFrame).updateSSHConnectedInfoMenu(status);
		else
			mainFrame.updateSSHConnectedInfoMenu(status);
	}

}