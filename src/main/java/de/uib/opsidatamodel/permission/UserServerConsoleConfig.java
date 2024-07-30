/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uib.utils.logging.Logging;

public class UserServerConsoleConfig extends UserConfigModule {

	public static final String KEY_TERMINAL_ACCESS_FORBIDDEN = "connect.terminal.forbidden";

	public static final String KEY_SERVER_CONSOLE_MENU_ACTIVE = "ssh.menu_serverconsole.active";
	public static final String KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE = "ssh.commandmanagement.active";
	public static final String KEY_SERVER_CONSOLE_COMMANDS_ACTIVE = "ssh.commands.active";
	public static final Set<String> BOOL_KEYS = Set.of(KEY_SERVER_CONSOLE_MENU_ACTIVE,
			KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE, KEY_SERVER_CONSOLE_COMMANDS_ACTIVE);

	public static final String KEY_OPT_CONFIGSERVER = "ConfigServer";
	public static final String KEY_OPT_DEPOTS = "Depots";
	public static final String KEY_OPT_CLIENTS = "Clients";
	public static final List<Object> FORBIDDEN_OPTIONS = List.of(KEY_OPT_CONFIGSERVER, KEY_OPT_DEPOTS, KEY_OPT_CLIENTS);
	public static final List<String> LIST_KEYS = List.of(KEY_TERMINAL_ACCESS_FORBIDDEN);

	public static final UserServerConsoleConfig DEFAULT;
	static {
		Logging.info("init ARCHEO_ for UserServerConsoleConfig");
		DEFAULT = new UserServerConsoleConfig(UserConfig.ARCHEO_ROLE_NAME);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_MENU_ACTIVE, true);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE, false);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_COMMANDS_ACTIVE, true);

		DEFAULT.setValues(KEY_TERMINAL_ACCESS_FORBIDDEN, new ArrayList<>());
		DEFAULT.setPossibleValues(KEY_TERMINAL_ACCESS_FORBIDDEN, FORBIDDEN_OPTIONS);
	}

	public UserServerConsoleConfig(String userName) {
		super(userName);
		super.setValues(KEY_TERMINAL_ACCESS_FORBIDDEN, new ArrayList<>());
		super.setPossibleValues(KEY_TERMINAL_ACCESS_FORBIDDEN, FORBIDDEN_OPTIONS);

		Logging.info(this.getClass(), "create UserServerConsoleConfig for user named ", userName,
				" with default values ", super.getBooleanMap(), " -- ", super.getValuesMap());
	}
}
