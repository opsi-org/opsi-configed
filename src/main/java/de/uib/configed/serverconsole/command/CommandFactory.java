/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private List<Map<String, Object>> commandlist;
	private List<MultiCommandTemplate> sshCommandList;
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

	public SingleCommand[] getDefaultOpsiCommands() {
		return DEFAULT_OPSI_COMMANDS;
	}

	public List<MultiCommandTemplate> retrieveCommandList() {
		Logging.info(this, "retrieveSSHCommandList ");
		if (commandlist == null) {
			commandlist = persistenceController.getSSHCommandDataService().retrieveCommandList();
		}

		sshCommandList = new ArrayList<>();
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

			Logging.info(this, "parent menu text " + parent);

			if (parent == null || "null".equalsIgnoreCase(parent) || parent.equals(PARENT_DEFAULT_FOR_OWN_COMMANDS)) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;
			}
			if (!knownParents.contains(parent)) {
				knownParents.add(parent);
			}

			Logging.info(this, "parent menu text changed  " + parent);

			Logging.info(this, "list_knownParents " + knownParents);

			sshCommandList.add(com);
		}
		return sshCommandList;
	}

	public Map<String, List<MultiCommandTemplate>> getCommandMapSortedByParent() {
		if (commandlist == null) {
			commandlist = persistenceController.getSSHCommandDataService().retrieveCommandList();
		}

		Logging.info(this, "getSSHCommandMapSortedByParent sorting commands ");
		Collections.sort(sshCommandList);

		Map<String, List<MultiCommandTemplate>> sortedComs = new LinkedHashMap<>();

		sortedComs.put(PARENT_DEFAULT_FOR_OWN_COMMANDS, new LinkedList<>());
		sortedComs.put(PARENT_OPSI, new LinkedList<>());

		for (MultiCommandTemplate com : sshCommandList) {
			String parent = com.getParentMenuText();
			if (parent == null || parent.isBlank()) {
				parent = PARENT_DEFAULT_FOR_OWN_COMMANDS;
			}
			List<MultiCommandTemplate> parentList = new LinkedList<>();
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

	public static MultiCommandTemplate buildSSHCommand(String id, String pmt, String mt, String ttt, int p,
			List<String> c) {
		return new MultiCommandTemplate(id, c, mt, pmt, ttt, p);
	}
}
