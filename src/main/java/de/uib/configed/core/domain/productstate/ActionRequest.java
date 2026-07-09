/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.productstate;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;

public enum ActionRequest {
	// conflicting entries from several clients
	CONFLICT(Globals.CONFLICT_STATE_STRING, null),

	// no valid entry from service
	INVALID(Globals.NO_VALID_STATE_STRING, null),

	// product offers no actions
	NOT_AVAILABLE(ActionRequest.KEY_NOT_AVAILABLE, null),

	// valid service states; declaration order is relevant, real actions come after NONE
	NONE(ActionRequest.KEY_NONE, Globals.ACTION_REQUEST_NONE_COLOR),
	SETUP(ActionRequest.KEY_SETUP, Globals.ACTION_REQUEST_SETUP_COLOR),
	UPDATE(ActionRequest.KEY_UPDATE, Globals.ACTION_REQUEST_UPDATE_COLOR),
	UNINSTALL(ActionRequest.KEY_UNINSTALL, Globals.ACTION_REQUEST_UNINSTALL_COLOR),
	ALWAYS(ActionRequest.KEY_ALWAYS, Globals.ACTION_REQUEST_ALWAYS_COLOR),
	ONCE(ActionRequest.KEY_ONCE, Globals.ACTION_REQUEST_ONCE_COLOR),
	CUSTOM(ActionRequest.KEY_CUSTOM, Globals.ACTION_REQUEST_CUSTOM_COLOR);

	public static final String KEY = "actionRequest";

	public static final String KEY_NONE = "none";
	public static final String KEY_SETUP = "setup";
	public static final String KEY_UPDATE = "update";
	public static final String KEY_UNINSTALL = "uninstall";
	public static final String KEY_ALWAYS = "always";
	public static final String KEY_ONCE = "once";
	public static final String KEY_CUSTOM = "custom";
	public static final String KEY_NOT_AVAILABLE = "not_available";

	private static final List<ActionRequest> SCRIPT_ACTIONS = List.of(SETUP, UPDATE, UNINSTALL, ALWAYS, ONCE, CUSTOM);

	private final String label;
	private final Color textColor;

	ActionRequest(String label, Color textColor) {
		this.label = label;
		this.textColor = textColor;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

	/**
	 * true for a real action (setup, update, uninstall, always, once, custom)
	 */
	public boolean isRealAction() {
		return compareTo(NONE) > 0;
	}

	public static ActionRequest fromLabel(String label) {
		return Utils.fromLabel(values(), ActionRequest::getLabel, label, NONE, INVALID);
	}

	public static String produceFromLabel(String label) {
		return fromLabel(label).getLabel();
	}

	public static List<String> getScriptKeys() {
		return SCRIPT_ACTIONS.stream().map(action -> action.label + "Script").toList();
	}

	public static Map<String, String> getScriptKey2Label() {
		Map<String, String> scriptKey2label = new LinkedHashMap<>();
		for (ActionRequest action : SCRIPT_ACTIONS) {
			scriptKey2label.put(action.label + "Script", action.label);
		}

		return scriptKey2label;
	}

	public static Map<String, Color> getLabel2TextColor() {
		Map<String, Color> label2textColor = new HashMap<>();
		for (ActionRequest action : values()) {
			if (action.textColor != null) {
				label2textColor.put(action.label, action.textColor);
			}
		}

		return label2textColor;
	}

	public static List<String> getLabels() {
		return List.of(KEY_NOT_AVAILABLE, KEY_NONE, KEY_SETUP, KEY_UPDATE, KEY_UNINSTALL, KEY_ALWAYS, KEY_ONCE,
				KEY_CUSTOM);
	}

	public static String[] getDisplayLabelsForChoice() {
		return new String[] { KEY_NONE, KEY_SETUP, KEY_UPDATE, KEY_UNINSTALL, KEY_ALWAYS, KEY_ONCE, KEY_CUSTOM };
	}
}
