/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

import de.uib.configed.Configed;

public final class TerminalCommandFactory {
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

	private static TerminalCommandFactory instance;

	private static final TerminalCommand[] DEFAULT_OPSI_COMMANDS = new TerminalCommand[] {
			new TerminalCommandPackageUpdater(), new TerminalCommandOpsiPackageManagerInstall(),
			new TerminalCommandOpsiPackageManagerUninstall(), new TerminalCommandOpsiMakeProductFile(),
			new TerminalCommandCurl(), new TerminalCommandModulesUpload(), new TerminalCommandOpsiSetRights(),
			new TerminalCommandDeployClientAgent() };

	public static TerminalCommandFactory getInstance() {
		if (instance == null) {
			instance = new TerminalCommandFactory();
		}
		return instance;
	}

	public static void destroyInstance() {
		instance = null;
	}

	public TerminalCommand[] getDefaultOpsiCommands() {
		return DEFAULT_OPSI_COMMANDS;
	}
}
