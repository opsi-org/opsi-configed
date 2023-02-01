package de.uib.opsidatamodel.permission;

import java.util.LinkedHashSet;
import java.util.Set;

import de.uib.utilities.logging.Logging;

public class UserSshConfig extends UserConfigModule {

	// not create new user specific ssh configs (use defaults)

	public static final String KEY_SSH_MENU_ACTIVE = "ssh.menu_serverconsole.active";

	public static final String KEY_SSH_CONFIG_ACTIVE = "ssh.serverconfiguration.active";

	public static final String KEY_SSH_COMMANDCONTROL_ACTIVE = "ssh.commandmanagement.active";

	public static final String KEY_SSH_COMMANDS_ACTIVE = "ssh.commands.active";
	// activate menus of all executable commands in menu server-console

	// addToBoolKeys
	public static final Set<String> BOOL_KEYS = new LinkedHashSet<>();
	static {
		BOOL_KEYS.add(KEY_SSH_MENU_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_CONFIG_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_COMMANDCONTROL_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_COMMANDS_ACTIVE);

		Logging.info(" UserSshConfig BOOL_KEYS " + BOOL_KEYS);
	}

	// TODO this set is empty, nowhere elements will be added... what to do with it?
	public static final Set<String> LIST_KEYS = new LinkedHashSet<>();

	public static final UserSshConfig DEFAULT;

	static {
		Logging.info("init ARCHEO_ for UserSshConfig");
		DEFAULT = new UserSshConfig(UserConfig.ARCHEO_ROLE_NAME);
		DEFAULT.setBooleanValue(KEY_SSH_MENU_ACTIVE, true);
		DEFAULT.setBooleanValue(KEY_SSH_CONFIG_ACTIVE, true);
		DEFAULT.setBooleanValue(KEY_SSH_COMMANDCONTROL_ACTIVE, false);
		DEFAULT.setBooleanValue(KEY_SSH_COMMANDS_ACTIVE, true);
	}

	public UserSshConfig(String userName) {
		super(userName);
	}

	public UserSshConfig(String userName, UserConfigModule prototype) {
		super(userName, prototype);
		Logging.info(this, "created for username " + userName + " with " + getBooleanMap() + " -- " + getValuesMap());
	}

}
