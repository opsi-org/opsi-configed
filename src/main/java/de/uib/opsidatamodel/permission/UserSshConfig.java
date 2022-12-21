package de.uib.opsidatamodel.permission;

import java.util.HashMap;
import java.util.LinkedHashSet;

import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class UserSshConfig extends UserConfigModule {
	// public static final String KEY_USER_SSH_REGISTER = "register.ssh";
	// public static final String KEY_USER_SSH_REGISTER_description = "if false: do
	// not create new user specific ssh configs (use defaults)";
	// public static boolean KEY_USER_SSH_REGISTER_defaultvalue = false;

	public static final String KEY_SSH_MENU_ACTIVE = "ssh.menu_serverconsole.active";
	private static final String KEY_SSH_MENU_ACTIVE_description = configed
			.getResourceValue("PersistenceController.SSH_MENU_ACTIVE");
	// "activates the whole menu server-console" ;
	// private static boolean KEY_SSH_MENU_ACTIVE_defaultvalue = true;

	public static final String KEY_SSH_CONFIG_ACTIVE = "ssh.serverconfiguration.active";
	private static final String KEY_SSH_CONFIG_ACTIVE_description = configed
			.getResourceValue("PersistenceController.SSH_CONFIG_ACTIVE");
	// "activates menu to configure ssh connection" ;
	// private static boolean KEY_SSH_CONFIG_ACTIVE_defaultvalue = true;

	public static final String KEY_SSH_COMMANDCONTROL_ACTIVE = "ssh.commandmanagement.active";
	private static final String KEY_SSH_COMMANDCONTROL_ACTIVE_description = configed
			.getResourceValue("PersistenceController.SSH_CONTROL_ACTIVE");
	// "activate the menu of command management";
	// private static boolean KEY_SSH_COMMANDCONTROL_ACTIVE_defaultvalue = false;

	// public static final String KEY_SSH_SHELL_ACTIVE = "ssh.terminal.active";
	// public static final String KEY_SSH_SHELL_ACTIVE_description =
	// configed.getResourceValue("PersistenceController.SSH_SHELL_ACTIVE");
	// = "activates terminal to ssh server";
	// public static boolean KEY_SSH_SHELL_ACTIVE_defaultvalue = false;

	public static final String KEY_SSH_COMMANDS_ACTIVE = "ssh.commands.active";
	private static final String KEY_SSH_COMMANDS_ACTIVE_description = configed
			.getResourceValue("PersistenceController.SSH_COMMANDS_ACTIVE");
	// activate menus of all executable commands in menu server-console";
	// private static boolean KEY_SSH_COMMANDS_ACTIVE_defaultvalue = true;

	// addToBoolKeys
	public static LinkedHashSet<String> BOOL_KEYS;
	{
		BOOL_KEYS = new LinkedHashSet<String>();
		BOOL_KEYS.add(KEY_SSH_MENU_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_CONFIG_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_COMMANDCONTROL_ACTIVE);
		BOOL_KEYS.add(KEY_SSH_COMMANDS_ACTIVE);

		logging.info(" UserSshConfig BOOL_KEYS " + BOOL_KEYS);
	}

	public static final LinkedHashSet<String> LIST_KEYS;
	static {
		LIST_KEYS = new LinkedHashSet<String>();
	}

	public static HashMap<String, String> configDescription;
	static {
		configDescription = new HashMap<>();
		configDescription.put(KEY_SSH_MENU_ACTIVE, KEY_SSH_MENU_ACTIVE_description);
		configDescription.put(KEY_SSH_CONFIG_ACTIVE, KEY_SSH_CONFIG_ACTIVE_description);
		configDescription.put(KEY_SSH_COMMANDCONTROL_ACTIVE, KEY_SSH_COMMANDCONTROL_ACTIVE_description);
		configDescription.put(KEY_SSH_COMMANDS_ACTIVE, KEY_SSH_COMMANDS_ACTIVE_description);
	}

	public static final UserSshConfig DEFAULT;

	static {
		logging.info("init ARCHEO_ for UserSshConfig");
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
		logging.info(this, "created for username " + userName + " with " + booleanMap + " -- " + valuesMap);
	}

}
