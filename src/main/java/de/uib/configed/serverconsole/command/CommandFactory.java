/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public final class CommandFactory {
	public static final String STRING_REPLACEMENT_DIRECTORY = "*.dir.*";
	public static final String STRING_COMMAND_GET_LINUX_COMMANDS = "COMMANDS=`echo -n $PATH "
			+ "| xargs -d : -I {} find {} -maxdepth 1 -executable -type f -printf '%P\\n'` ;"
			+ " ALIASES=`alias | cut -d '=' -f 1`; echo \"$COMMANDS\"$'\\n'\"$ALIASES\" | sort -u ";
	public static final String STRING_COMMAND_GET_DIRECTORIES = "ls --color=never -d *.dir.*/*/";
	public static final String STRING_COMMAND_GET_OPSI_FILES = "ls --color=never *.dir.*/*.opsi";
	public static final String STRING_COMMAND_GET_VERSIONS = "grep version: *.dir.* --max-count=2  ";
	public static final String STRING_COMMAND_CAT_DIRECTORY = "cat *.dir.*OPSI/control | grep \"id: \"";

	public static final String OPSI_PATH_VAR_REPOSITORY = "/var/lib/opsi/repository/";
	public static final String OPSI_PATH_VAR_DEPOT = "/var/lib/opsi/depot/";
	public static final String WEBDAV_OPSI_PATH_VAR_DEPOT = "depot/";
	public static final String WEBDAV_OPSI_PATH_VAR_REPOSITORY = "repository/";
	public static final String WEBDAV_OPSI_PATH_VAR_WORKBENCH = "workbench/";

	public static final String PARENT_NULL = Configed.getResourceValue("MainFrame.jMenuServer");
	public static final String PARENT_DEFAULT_FOR_OWN_COMMANDS = "...";
	public static final String PARENT_OPSI = Configed.getResourceValue("MainFrame.jMenuOpsi");
	public static final String MENU_NEW = Configed.getResourceValue("SSHConnection.CommandControl.menuText_newCommand");
	public static final int DEFAULT_POSITION = 0;

	public static final String COMMAND_MAP_ID = "id";
	public static final String COMMAND_MAP_MENU_TEXT = "menuText";
	public static final String COMMAND_MAP_PARENT_MENU_TEXT = "parentMenuText";
	public static final String COMMAND_MAP_TOOLTIP_TEXT = "tooltipText";
	public static final String COMMAND_MAP_POSITION = "position";
	public static final String COMMAND_MAP_COMMANDS = "commands";

	public static final String CONFIDENTIAL = "***confidential***";

	private static CommandFactory instance;

	private static final SingleCommand[] DEFAULT_OPSI_COMMANDS = new SingleCommand[] {
			new SingleCommandPackageUpdater(), new SingleCommandOpsiPackageManagerInstall(),
			new SingleCommandOpsiPackageManagerUninstall(), new SingleCommandOpsiMakeProductFile(),
			new SingleCommandCurl(), new SingleCommandModulesUpload(), new SingleCommandOpsiSetRights(),
			new SingleCommandDeployClientAgent() };

	private List<MultiCommandTemplate> commandList;
	private Set<String> knownMenus;
	private Set<String> knownParents;

	private CommandParameterMethods pmethodHandler;
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public CommandFactory(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		this.pmethodHandler = new CommandParameterMethods(configedMain);
	}

	public static CommandFactory getInstance(ConfigedMain configedMain) {
		if (instance == null) {
			instance = new CommandFactory(configedMain);
		}
		return instance;
	}

	public static void destroyInstance() {
		instance = null;
	}

	public CommandParameterMethods getParameterMethodHandler() {
		if (pmethodHandler == null) {
			pmethodHandler = new CommandParameterMethods(configedMain);
		}
		return pmethodHandler;
	}

	public static SingleCommand[] getDefaultOpsiCommands() {
		return DEFAULT_OPSI_COMMANDS;
	}

	public List<MultiCommandTemplate> retrieveCommandList() {
		List<Map<String, Object>> commandlist = persistenceController.getSSHCommandDataService().retrieveCommandList();

		commandList = new ArrayList<>();
		knownMenus = new HashSet<>();
		knownParents = new HashSet<>();

		if (!commandlist.isEmpty()) {
			knownParents.add(PARENT_DEFAULT_FOR_OWN_COMMANDS);
		}

		knownMenus.add(PARENT_DEFAULT_FOR_OWN_COMMANDS);

		for (Map<String, Object> map : commandlist) {
			MultiCommandTemplate com = buildSSHCommand((String) map.get(COMMAND_MAP_ID),
					(String) map.get(COMMAND_MAP_PARENT_MENU_TEXT), (String) map.get(COMMAND_MAP_MENU_TEXT),
					(String) map.get(COMMAND_MAP_TOOLTIP_TEXT), (int) map.get(COMMAND_MAP_POSITION), null);
			if (map.get(COMMAND_MAP_COMMANDS) != null) {
				List<String> commandCommands = new LinkedList<>(
						POJOReMapper.remap(map.get(COMMAND_MAP_COMMANDS), new TypeReference<List<String>>() {
						}));

				com.setCommands(commandCommands);
			}
			knownMenus.add(com.getMenuText());

			String parent = com.getParentMenuText();

			if (parent == null || "null".equalsIgnoreCase(parent) || parent.equals(PARENT_DEFAULT_FOR_OWN_COMMANDS)) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;
			}
			if (!knownParents.contains(parent)) {
				knownParents.add(parent);
			}

			commandList.add(com);
		}
		return commandList;
	}

	public Map<String, List<MultiCommandTemplate>> getCommandMapSortedByParent() {
		Logging.info(this, "getCommandMapSortedByParent sorting commands");
		Collections.sort(commandList);

		Map<String, List<MultiCommandTemplate>> sortedCommands = new LinkedHashMap<>();
		sortedCommands.put(PARENT_DEFAULT_FOR_OWN_COMMANDS, new LinkedList<>());
		sortedCommands.put(PARENT_OPSI, new LinkedList<>());

		for (MultiCommandTemplate command : commandList) {
			String parent = command.getParentMenuText();
			if (parent == null || parent.isBlank()) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;
			}
			List<MultiCommandTemplate> parentList = new LinkedList<>();
			if (sortedCommands.containsKey(parent)) {
				parentList = sortedCommands.get(parent);
			} else {
				sortedCommands.put(parent, parentList);
			}

			if (!(parentList.contains(command))) {
				parentList.add(command);
			}
		}
		return sortedCommands;
	}

	public static MultiCommandTemplate buildSSHCommand(String id, String pmt, String mt, String ttt, int p,
			List<String> c) {
		return new MultiCommandTemplate(id, c, mt, pmt, ttt, p);
	}

	public MultiCommandTemplate getCommandByMenu(String menu) {
		for (MultiCommandTemplate c : commandList) {
			if (c.getMenuText().equals(menu)) {
				return c;
			}
		}
		return null;
	}

	public List<String> getCommandMenuNames() {
		List<String> knownMenusList = new ArrayList<>(knownMenus);
		Collections.sort(knownMenusList, String::compareToIgnoreCase);
		return knownMenusList;
	}

	public List<String> getCommandMenuParents() {
		List<String> knownParentsList = new ArrayList<>(knownParents);
		Collections.sort(knownParentsList, String::compareToIgnoreCase);
		return knownParentsList;
	}

	public boolean saveCommand(MultiCommandTemplate command) {
		Logging.info(this, "saveCommand command " + command.toString());
		List<Object> jsonObjects = new ArrayList<>();
		jsonObjects.add(buildCommandMap(command));

		if (knownMenus.contains(command.getMenuText())) {
			Logging.info(this, "saveCommand command already exists - updating command");
			if (persistenceController.getSSHCommandDataService().updateSSHCommand(jsonObjects)) {
				commandList.get(commandList.indexOf(getCommandByMenu(command.getMenuText()))).update(command);
				return true;
			}
		} else {
			Logging.info(this, "saveCommand command doesn't exist - creating new command");
			if (persistenceController.getSSHCommandDataService().createSSHCommand(jsonObjects)) {
				commandList.add(command);
				knownMenus.add(command.getMenuText());
				return true;
			}
		}
		return false;
	}

	private static Map<String, Object> buildCommandMap(MultiCommandTemplate c) {
		Map<String, Object> com = new HashMap<>();
		com.put(COMMAND_MAP_MENU_TEXT, c.getMenuText());
		com.put(COMMAND_MAP_PARENT_MENU_TEXT, c.getParentMenuText());
		com.put(COMMAND_MAP_TOOLTIP_TEXT, c.getToolTipText());
		com.put(COMMAND_MAP_POSITION, c.getPriority());
		com.put(COMMAND_MAP_COMMANDS, c.getCommandsRaw());
		return com;
	}

	public boolean isCommandEqualSavedCommand(MultiCommandTemplate command) {
		boolean result = false;
		if (knownMenus.contains(command.getMenuText())) {
			Logging.debug(this, "isCommandEqualSavedCommand comparing command " + command.toString());
			if (commandList == null) {
				Logging.debug(this, "isCommandEqualSavedCommand command list is null");
				result = false;
			} else if (commandList.isEmpty()) {
				Logging.debug(this, "isCommandEqualSavedCommand command list has no elements");
				result = false;
			} else if (getCommandByMenu(command.getMenuText()) == null) {
				Logging.debug(this, "isCommandEqualSavedCommand command has no menu text");
				result = false;
			} else if (commandList.indexOf(getCommandByMenu(command.getMenuText())) == -1) {
				Logging.debug(this, "isCommandEqualSavedCommand command is new");
				result = false;
			} else {
				Logging.debug(this, "isCommandEqualSavedCommand command list " + commandList);
				Logging.debug(this, "isCommandEqualSavedCommand found command "
						+ commandList.get(commandList.indexOf(getCommandByMenu(command.getMenuText()))));
				Logging.debug(this, "isCommandEqualSavedCommand are commands equal? " + commandList
						.get(commandList.indexOf(getCommandByMenu(command.getMenuText()))).equals(command));
				result = commandList.get(commandList.indexOf(getCommandByMenu(command.getMenuText()))).equals(command);
			}
		}
		return result;
	}

	public void deleteCommandByMenu(String menu) {
		Logging.info(this, "deleting command menu " + menu);
		List<String> jsonObjects = new ArrayList<>();
		jsonObjects.add(menu);
		if (persistenceController.getSSHCommandDataService().deleteSSHCommand(jsonObjects)) {
			Iterator<MultiCommandTemplate> iterator = commandList.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(getCommandByMenu(menu))) {
					iterator.remove();
				}
			}
			knownMenus.remove(menu);
		}
	}

	public void reloadServerMenu() {
		SwingUtilities.invokeLater(configedMain::reloadServerMenu);
	}
}
