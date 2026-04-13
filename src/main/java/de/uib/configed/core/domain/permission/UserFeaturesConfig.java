/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uib.configed.share.logging.Logging;

public class UserFeaturesConfig extends UserConfigModule {
	public static final String KEY_MOTD_ACCESS_FORBIDDEN = "message_of_the_day.forbidden";
	public static final String KEY_OPT_MOTD_DEVICE = "Device";
	public static final String KEY_OPT_MOTD_USER = "User";

	public static final Set<String> BOOL_KEYS = Set.of();

	public static final List<Object> FORBIDDEN_OPTIONS = List.of(KEY_OPT_MOTD_DEVICE, KEY_OPT_MOTD_USER);
	public static final List<String> LIST_KEYS = List.of(KEY_MOTD_ACCESS_FORBIDDEN);

	public static final UserFeaturesConfig DEFAULT;
	static {
		Logging.info("init ARCHEO_ for UserFeaturesConfig");
		DEFAULT = new UserFeaturesConfig(UserConfig.ARCHEO_ROLE_NAME);

		DEFAULT.setValues(KEY_MOTD_ACCESS_FORBIDDEN, new ArrayList<>());
		DEFAULT.setPossibleValues(KEY_MOTD_ACCESS_FORBIDDEN, FORBIDDEN_OPTIONS);
	}

	public UserFeaturesConfig(String userName) {
		super(userName);
		super.setValues(KEY_MOTD_ACCESS_FORBIDDEN, new ArrayList<>());
		super.setPossibleValues(KEY_MOTD_ACCESS_FORBIDDEN, FORBIDDEN_OPTIONS);

		Logging.info(this.getClass(), "create UserFeaturesConfig for user named ", userName, " with default values ",
				super.getBooleanMap(), " -- ", super.getValuesMap());
	}
}
