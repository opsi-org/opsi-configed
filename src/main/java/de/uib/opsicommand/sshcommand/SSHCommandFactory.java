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

import de.uib.configed.Configed;
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
import de.uib.configed.gui.MainFrame;
import de.uib.utilities.logging.Logging;

/**
 * This Class handles SSHCommands.
 **/
public final class SSHCommandFactory {
	/** final string commands for linux terminal **/
	public static final String STRING_REPLACEMENT_DIRECTORY = "*.dir.*";
	// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
	public static final String STRING_COMMAND_GET_LINUX_COMMANDS = "COMMANDS=`echo -n $PATH | xargs -d : -I {} find {} -maxdepth 1 -executable -type f -printf '%P\\n'` ; ALIASES=`alias | cut -d '=' -f 1`; echo \"$COMMANDS\"$'\\n'\"$ALIASES\" | sort -u ";
	public static final String STRING_COMMAND_GET_DIRECTORIES = "ls --color=never -d *.dir.*/*/";
	public static final String STRING_COMMAND_GET_OPSI_FILES = "ls --color=never *.dir.*/*.opsi";
	public static final String STRING_COMMAND_GET_VERSIONS = "grep version: *.dir.* --max-count=2  ";
	public static final String STRING_COMMAND_CAT_DIRECTORY = "cat *.dir.*OPSI/control | grep \"id: \"";
	public static final String STRING_COMMAND_FILE_EXISTS_NOT_REMOVE = "[ -d .filename. ] && echo \"File exists\" || echo \"File not exist\"";

	public static final String STRING_REPLACEMENT_FILENAME = ".filename.";

	public static final String OPSI_PATH_VAR_REPOSITORY = "/var/lib/opsi/repository/";
	public static final String OPSI_PATH_VAR_DEPOT = "/var/lib/opsi/depot/";

	/** static String for parent null ("Server-Konsole") **/
	public static final String PARENT_NULL = Configed.getResourceValue("MainFrame.jMenuServer");
	/**
	 * static String defined as language independent parent for own commands
	 **/
	public static final String PARENT_DEFAULT_FOR_OWN_COMMANDS = "...";
	/** static String for specific parent ("opsi") **/
	public static final String PARENT_OPSI = Configed.getResourceValue("MainFrame.jMenuOpsi");
	/** static String for new command ("<Neuer Befehl>") **/
	public static final String MENU_NEW = Configed.getResourceValue("SSHConnection.CommandControl.menuText_newCommand");
	/** default position is 0 **/
	public static final int POSITION_DEFAULT = 0;

	/** setting ssh_colored_output per default true **/
	public static boolean sshColoredOutput = true;
	/** setting ssh_always_exec_in_background per default false **/
	public static boolean sshAlwaysExecInBackground;
	/** all static commands which need run-time parameter **/
	private static List<SSHCommand> sshCommandsParam = new ArrayList<>();

	/** static final name of field "id" */
	public static final String COMMAND_MAP_ID = "id";
	/** static final name of field "menuText" */
	public static final String COMMAND_MAP_MENU_TEXT = "menuText";
	/** static final name of field "parentMenuText" */
	public static final String COMMAND_MAP_PARENT_MENU_TEXT = "parentMenuText";
	/** static final name of field "tooltipText" */
	public static final String COMMAND_MAP_TOOLTIP_TEXT = "tooltipText";
	/** static final name of field "position" */
	public static final String COMMAND_MAP_POSITION = "position";
	/** static final name of field "needSudo" */
	public static final String COMMAND_MAP_NEED_SUDO = "needSudo";
	/** static final name of field "commands" */
	public static final String COMMAND_MAP_COMMANDS = "commands";

	public static final String CONNECTED = Configed.getResourceValue("SSHConnection.connected");
	public static final String CONNECTION_NOT_ALLOWED = Configed
			.getResourceValue("SSHConnection.connected_not_allowed");
	public static final String UNKNOWN = Configed.getResourceValue("SSHConnection.unknown");
	public static final String NOT_CONNECTED = Configed.getResourceValue("SSHConnection.not_connected");

	// SSHCommandFactory.getInstance().sudo_text
	public static final String SUDO_FAILED_TEXT = Configed.getResourceValue("SSHConnection.sudoFailedText");
	public static final String SUDO_TEXT = "sudo -S -p \"" + SUDO_FAILED_TEXT + "\" ";

	public static final String SSH_USER = "<<!sshuser!>>";
	public static final String SSH_HOST = "<<!sshhost!>>";

	public static final String CONFIDENTIAL = "***confidential***";

	/** SSHCommandFactory instance **/
	private static SSHCommandFactory instance;

	Set<String> allowedHosts = new HashSet<>();

	SSHConnectExec connection;

	String connectionState = NOT_CONNECTED;

	/**
	 * List<Map<String,Object>> list elements are commands with key value pairs
	 **/
	private List<Map<String, Object>> commandlist;
	/** List<SSHCommand_Template> list elements are sshcommands **/
	private List<SSHCommandTemplate> sshCommandList;
	/** list of known menus **/
	private List<String> listKnownMenus;
	/** list of known parent menus **/
	private List<String> listKnownParents;

	/** ConfigedMain instance **/
	private ConfigedMain main;
	private MainFrame mainFrame;

	List<String> createdProducts = new ArrayList<>();

	SSHCommandParameterMethods pmethodHandler;
	SSHConnectionInfo connectionInfo;

	/**
	 * Factory Instance for SSH Command
	 * 
	 * @param main {@link de.uib.configed.ConfigedMain} class
	 **/
	private SSHCommandFactory(ConfigedMain main) {
		this.main = main;
		Logging.info(this, "SSHComandFactory new instance");
		instance = this;
		addAditionalParamCommands();
		connection = new SSHConnectExec(this.main);
		connectionInfo = SSHConnectionInfo.getInstance();
		pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @param configedMain {@link de.uib.configed.ConfigedMain} class
	 * @return SSHCommandFactory instance
	 **/
	public static SSHCommandFactory getInstance(ConfigedMain m) {
		if (instance != null) {
			return instance;
		} else {
			return new SSHCommandFactory(m);
		}
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @return SSHCommandFactory instance
	 **/
	public static SSHCommandFactory getInstance() {
		if (instance != null) {
			return instance;
		} else {
			return new SSHCommandFactory(null);
		}
	}

	public void setAllowedHosts(Collection<String> allowed) {
		allowedHosts.addAll(allowed);
	}

	public Set<String> getAllowedHosts() {
		return allowedHosts;
	}

	private static void addAditionalParamCommands() {

		sshCommandsParam.add(new CommandPackageUpdater());
		sshCommandsParam.add(new CommandOpsiPackageManagerInstall());
		sshCommandsParam.add(new CommandOpsiPackageManagerUninstall());
		sshCommandsParam.add(new CommandOpsimakeproductfile());
		sshCommandsParam.add(new CommandWget());
		sshCommandsParam.add(new CommandModulesUpload());
		sshCommandsParam.add(new CommandOpsiSetRights());
		sshCommandsParam.add(new CommandDeployClientAgent());

	}

	public List<String> getProductHistory() {
		return createdProducts;
	}

	public void addProductHistory(String prod) {
		createdProducts.add(prod);
	}

	public SSHCommandParameterMethods getParameterHandler() {
		if (pmethodHandler != null) {
			return pmethodHandler;
		} else {
			pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
			return pmethodHandler;
		}
	}

	public void setMainFrame(MainFrame mf) {
		if (mf != null) {
			mainFrame = mf;
		}
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
		Logging.info(this, "retrieveSSHCommandListRequestRefresh commandlist null");
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
	public static SSHCommandTemplate buildSSHCommand(String id, String pmt, String mt, String ttt, int p, boolean ns,
			List<String> c) {

		// Achtung Reihenfolge der Elemente in Arrays c könnte sich ändern !" toList =
		// ArrayList! JsonArray muss nicht sortiert sein!"
		return new SSHCommandTemplate(id, c, mt, ns, pmt, ttt, p);
	}

	/**
	 * retrieve commandlist from persistencecontroller (if commandlist is null)
	 * and build sshcommandlist
	 * 
	 * @return List<SSHCommand_Template>
	 **/
	public List<SSHCommandTemplate> retrieveSSHCommandList() {
		Logging.info(this, "retrieveSSHCommandList ");
		if (commandlist == null) {
			commandlist = main.getPersistenceController().retrieveCommandList();
		}

		sshCommandList = new ArrayList<>();
		listKnownMenus = new ArrayList<>();
		listKnownParents = new ArrayList<>();

		if (!commandlist.isEmpty()) {
			listKnownParents.add(PARENT_DEFAULT_FOR_OWN_COMMANDS);
		}

		listKnownMenus.add(PARENT_DEFAULT_FOR_OWN_COMMANDS);

		for (Map<String, Object> map : commandlist) {
			SSHCommandTemplate com = buildSSHCommand(((String) map.get(COMMAND_MAP_ID)),
					((String) map.get(COMMAND_MAP_PARENT_MENU_TEXT)), ((String) map.get(COMMAND_MAP_MENU_TEXT)),
					((String) map.get(COMMAND_MAP_TOOLTIP_TEXT)), ((int) map.get(COMMAND_MAP_POSITION)),
					((boolean) map.get(COMMAND_MAP_NEED_SUDO)), null);
			if (map.get(COMMAND_MAP_COMMANDS) != null) {
				// Achtung Reihenfolge könnte sich ändern !" toList = ArrayList! JsonArray muss
				// nicht sortiert sein!"
				List commandCommands = new LinkedList<>(((JSONArray) map.get(COMMAND_MAP_COMMANDS)).toList());
				commandCommands.add("echo ... ");
				commandCommands.add("echo READY");
				com.setCommands(commandCommands);
			}
			listKnownMenus.add(com.getMenuText());

			String parent = com.getParentMenuText();

			Logging.info(this, "parent menu text " + parent);

			if (parent == null || "null".equalsIgnoreCase(parent) || parent.equals(PARENT_DEFAULT_FOR_OWN_COMMANDS)) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;
			}
			if (!listKnownParents.contains(parent)) {
				listKnownParents.add(parent);
			}

			Logging.info(this, "parent menu text changed  " + parent);

			Logging.info(this, "list_knownParents " + listKnownParents);

			sshCommandList.add(com);
		}
		return sshCommandList;
	}

	/**
	 * Sort all menu names alphabeticly
	 * 
	 * @return List<String> sorted list_knownMenus
	 **/
	public List<String> getSSHCommandMenuNames() {
		if (commandlist == null) {
			commandlist = main.getPersistenceController().retrieveCommandList();
		}
		Collections.sort(listKnownMenus, String::compareToIgnoreCase);
		return listKnownMenus;
	}

	/**
	 * Sort all parent menus alphabeticly
	 * 
	 * @return List<String> sorted list_knownParents
	 **/
	public List<String> getSSHCommandMenuParents() {
		if (commandlist == null) {
			commandlist = main.getPersistenceController().retrieveCommandList();
		}
		Collections.sort(listKnownParents, String::compareToIgnoreCase);
		return listKnownParents;
	}

	/**
	 * Sorts all SSHCommands by position, after that sorts by there parent menus
	 * (keep position order in parent menus).
	 * 
	 * @return LinkedHashMap<String,List<SSHCommand_Template>> sortedComs
	 **/
	public Map<String, List<SSHCommandTemplate>> getSSHCommandMapSortedByParent() {
		if (commandlist == null) {
			commandlist = main.getPersistenceController().retrieveCommandList();
		}

		Logging.info(this, "getSSHCommandMapSortedByParent sorting commands ");
		Collections.sort(sshCommandList);

		LinkedHashMap<String, List<SSHCommandTemplate>> sortedComs = new LinkedHashMap<>();

		sortedComs.put(PARENT_DEFAULT_FOR_OWN_COMMANDS, new LinkedList<>());
		sortedComs.put(PARENT_OPSI, new LinkedList<>());

		for (SSHCommandTemplate com : sshCommandList) {
			String parent = com.getParentMenuText();
			if ((parent == null) || (parent.trim().isEmpty())) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;

			}
			List<SSHCommandTemplate> parentList = new LinkedList<>();
			if (sortedComs.containsKey(parent)) {
				parentList = sortedComs.get(parent);
			} else {
				sortedComs.put(parent, parentList);
			}

			if (!(parentList.contains(com))) {
				parentList.add(com);
			}
		}
		return sortedComs;
	}

	/**
	 * Get list of SSHCommands which need run-time parameter
	 * 
	 * @return List<SSHCommand> ssh_commands_param
	 **/
	public List<SSHCommand> getSSHCommandParameterList() {
		Logging.info(this, "getSSHCommandParameterList ");
		return sshCommandsParam;
	}

	/**
	 * Search sshcommand with given menu name
	 * 
	 * @param menu
	 * @return SSHCommand_Template
	 **/
	public SSHCommandTemplate getSSHCommandByMenu(String menu) {
		if (sshCommandList == null) {
			commandlist = main.getPersistenceController().retrieveCommandList();
		}
		for (SSHCommandTemplate c : sshCommandList) {
			if (c.getMenuText().equals(menu)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Build an Map of key-value-pairs from a SSHCommand_Template
	 * 
	 * @param SSHCommandTemplate
	 * @return Map<String,Object> command
	 **/
	private static Map<String, Object> buildCommandMap(SSHCommandTemplate c) {
		Map<String, Object> com = new HashMap<>();

		com.put(COMMAND_MAP_MENU_TEXT, c.getMenuText());
		com.put(COMMAND_MAP_PARENT_MENU_TEXT, c.getParentMenuText());
		com.put(COMMAND_MAP_TOOLTIP_TEXT, c.getToolTipText());
		com.put(COMMAND_MAP_POSITION, c.getPriority());
		com.put(COMMAND_MAP_NEED_SUDO, c.needSudo());
		return com;
	}

	/**
	 * Create or update an command (update local lists)
	 * 
	 * @param SSHCommandTemplate command
	 **/
	public boolean saveSSHCommand(SSHCommandTemplate command) {
		Logging.info(this, "saveSSHCommand command " + command.toString());
		List<Object> jsonObjects = new ArrayList<>();
		try {
			JSONObject jsComMap = new JSONObject(buildCommandMap(command));
			JSONArray jsComArrCom = new JSONArray(((SSHMultiCommand) command).getCommandsRaw());
			jsComMap.put(COMMAND_MAP_COMMANDS, jsComArrCom);
			jsonObjects.add(jsComMap);
		} catch (Exception e) {
			Logging.warning(this, "saveSSHCommand, exception occurred", e);
		}

		if (listKnownMenus.contains(command.getMenuText())) {
			Logging.info(this, "saveSSHCommand sshcommand_list.contains(command) true");
			if (main.getPersistenceController().updateSSHCommand(jsonObjects)) {
				sshCommandList.get(sshCommandList.indexOf(getSSHCommandByMenu(command.getMenuText()))).update(command);
				return true;
			}
		} else {
			Logging.info(this, "saveSSHCommand sshcommand_list.contains(command) false");
			if (main.getPersistenceController().createSSHCommand(jsonObjects)) {
				sshCommandList.add(command);
				listKnownMenus.add(command.getMenuText());
				return true;
			}
		}
		return false;
	}

	public boolean isSSHCommandEqualSavedCommand(SSHCommandTemplate command) {
		if (listKnownMenus.contains(command.getMenuText())) {
			Logging.info(this, "isSSHCommandEqualSavedCommand compare command " + command.toString());
			if (sshCommandList == null) {
				Logging.info(this, "isSSHCommandEqualSavedCommand  command_list == null ");
				return false;
			}
			if (sshCommandList.isEmpty()) {
				Logging.info(this, "isSSHCommandEqualSavedCommand  command_list has no elements ");
				return false;
			}

			if (getSSHCommandByMenu(command.getMenuText()) == null) {
				Logging.info(this,
						" isSSHCommandEqualSavedCommand getSSHCommandByMenu( command.getMenuText() ) is null ");
				return false;
			}

			Logging.info(this, "isSSHCommandEqualSavedCommand  command_list " + (sshCommandList));

			Logging.info(this, "isSSHCommandEqualSavedCommand with found "
					+ sshCommandList.get(sshCommandList.indexOf(getSSHCommandByMenu(command.getMenuText()))));
			Logging.info(this, "isSSHCommandEqualSavedCommand equals " + sshCommandList
					.get(sshCommandList.indexOf(getSSHCommandByMenu(command.getMenuText()))).equals(command));

			return sshCommandList.get(sshCommandList.indexOf(getSSHCommandByMenu(command.getMenuText())))
					.equals(command);
		}

		return false;
	}

	/**
	 * Delete the command with given menu text
	 * 
	 * @param String menu
	 **/
	public void deleteSSHCommandByMenu(String menu) {
		Logging.info(this, "deleteSSHCommand menu " + menu);
		// return
		List<String> jsonObjects = new ArrayList<>();
		jsonObjects.add(menu);
		if (main.getPersistenceController().deleteSSHCommand(jsonObjects)) {
			sshCommandList.remove(getSSHCommandByMenu(menu));
			listKnownMenus.remove(menu);
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
		SSHCommand command = new EmptyCommand(EmptyCommand.TESTCOMMAND, EmptyCommand.TESTCOMMAND, "", false);
		connectionState = UNKNOWN;
		try {
			if (connection.connect(command)) {
				String result = connection.exec(command, false);
				Logging.info(this, "connection.exec produces " + result);
				if (result == null || result.trim().isEmpty()) {
					Logging.info(this, "testConnection not allowed");
					connectionState = CONNECTION_NOT_ALLOWED;
					Logging.warning(this, "cannot connect to " + user + "@" + host);
				} else {
					Logging.info(this, "testConnection connected");
					connectionState = CONNECTED;
				}
			} else {
				Logging.info(this, "testConnection not connected");
				connectionState = NOT_CONNECTED;
				Logging.warning(this, "cannot connect to " + user + "@" + host);
			}

		} catch (Exception e) {
			Logging.info(this, "testConnection not connected");
			connectionState = NOT_CONNECTED;
			Logging.warning(this, "cannot connect to " + user + "@" + host, e);
		}
		updateConnectionInfo(connectionState);
		Logging.info(this, "testConnection connection state " + connectionState);
	}

	public void updateConnectionInfo(String status) {
		Logging.info(this, "mainFrame " + mainFrame);
		Logging.info(this, "ConfigedMain.getMainFrame() " + ConfigedMain.getMainFrame());

		Logging.info(this, "status " + status);
		if (mainFrame == null) {
			ConfigedMain.getMainFrame().updateSSHConnectedInfoMenu(status);
		} else {
			mainFrame.updateSSHConnectedInfoMenu(status);
		}
	}
}
