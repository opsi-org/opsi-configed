/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import java.util.Set;

import de.uib.utils.logging.Logging;

public class UserServerConsoleConfig extends UserConfigModule {
	public static final String KEY_SERVER_CONSOLE_MENU_ACTIVE = "ssh.menu_serverconsole.active";
	public static final String KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE = "ssh.commandmanagement.active";
	public static final String KEY_SERVER_CONSOLE_COMMANDS_ACTIVE = "ssh.commands.active";
	public static final Set<String> BOOL_KEYS = Set.of(KEY_SERVER_CONSOLE_MENU_ACTIVE,
			KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE, KEY_SERVER_CONSOLE_COMMANDS_ACTIVE);
	public static final UserServerConsoleConfig DEFAULT;
	static {
		Logging.info("init ARCHEO_ for UserSshConfig");
		DEFAULT = new UserServerConsoleConfig(UserConfig.ARCHEO_ROLE_NAME);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_MENU_ACTIVE, true);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE, false);
		DEFAULT.setBooleanValue(KEY_SERVER_CONSOLE_COMMANDS_ACTIVE, true);
	}

	public UserServerConsoleConfig(String userName) {
		super(userName);
	}
}
