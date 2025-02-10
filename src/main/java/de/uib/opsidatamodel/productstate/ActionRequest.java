/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uib.configed.Globals;

public class ActionRequest {
	public static final String KEY = "actionRequest";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// product offers no actions
	public static final int NOT_AVAILABLE = -6;

	public static final String KEY_NONE = "none";
	public static final String KEY_SETUP = "setup";
	public static final String KEY_UPDATE = "update";
	public static final String KEY_UNINSTALL = "uninstall";
	public static final String KEY_ALWAYS = "always";
	public static final String KEY_ONCE = "once";
	public static final String KEY_CUSTOM = "custom";
	public static final String KEY_NOT_AVAILABLE = "not_available";

	// valid service states
	public static final int NONE = 0;
	public static final int SETUP = 1;
	public static final int UPDATE = 3;
	public static final int UNINSTALL = 5;
	public static final int ALWAYS = 7;
	public static final int ONCE = 8;
	public static final int CUSTOM = 11;
	public static final int LAST = CUSTOM;

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, Color> label2textColor;

	private static Set<String> labels;
	private static String[] choiceLabels;
	private static List<String> scriptKeys;

	private static Map<String, String> scriptKey2label;

	// instance variable
	private int state = INVALID;

	public ActionRequest(int t) {
		state = t;
	}

	private static void checkCollections() {
		if (labels != null) {
			return;
		}

		labels = new LinkedHashSet<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add(KEY_NOT_AVAILABLE);
		labels.add(KEY_NONE);
		labels.add(KEY_SETUP);
		labels.add(KEY_UPDATE);
		labels.add(KEY_UNINSTALL);
		labels.add(KEY_ALWAYS);
		labels.add(KEY_ONCE);
		labels.add(KEY_CUSTOM);

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(NOT_AVAILABLE, KEY_NOT_AVAILABLE);
		state2label.put(NONE, KEY_NONE);
		state2label.put(SETUP, KEY_SETUP);
		state2label.put(UPDATE, KEY_UPDATE);
		state2label.put(UNINSTALL, KEY_UNINSTALL);
		state2label.put(ALWAYS, KEY_ALWAYS);
		state2label.put(ONCE, KEY_ONCE);
		state2label.put(CUSTOM, KEY_CUSTOM);

		Map<String, Integer> serviceValue2state = new HashMap<>();
		serviceValue2state.put(KEY_SETUP, SETUP);
		serviceValue2state.put(KEY_UPDATE, UPDATE);
		serviceValue2state.put(KEY_UNINSTALL, UNINSTALL);
		serviceValue2state.put(KEY_ALWAYS, ALWAYS);
		serviceValue2state.put(KEY_ONCE, ONCE);
		serviceValue2state.put(KEY_CUSTOM, CUSTOM);

		scriptKeys = new ArrayList<>();

		scriptKey2label = new HashMap<>();

		for (Entry<String, Integer> stateEntry : serviceValue2state.entrySet()) {
			scriptKeys.add(stateEntry.getKey() + "Script");

			scriptKey2label.put(stateEntry.getKey() + "Script", state2label.get(stateEntry.getValue()));
		}

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put(KEY_NOT_AVAILABLE, NOT_AVAILABLE);
		label2state.put(KEY_NONE, NONE);
		label2state.put(KEY_SETUP, SETUP);
		label2state.put(KEY_UPDATE, UPDATE);
		label2state.put(KEY_UNINSTALL, UNINSTALL);
		label2state.put(KEY_ALWAYS, ALWAYS);
		label2state.put(KEY_ONCE, ONCE);
		label2state.put(KEY_CUSTOM, CUSTOM);

		choiceLabels = new String[] { KEY_NONE, KEY_SETUP, KEY_UPDATE, KEY_UNINSTALL, KEY_ALWAYS, KEY_ONCE,
				KEY_CUSTOM, };

		label2textColor = new HashMap<>();
		label2textColor.put(KEY_NONE, Globals.ACTION_REQUEST_NONE_COLOR);
		label2textColor.put(KEY_SETUP, Globals.ACTION_REQUEST_SETUP_COLOR);
		label2textColor.put(KEY_UPDATE, Globals.ACTION_REQUEST_UPDATE_COLOR);
		label2textColor.put(KEY_UNINSTALL, Globals.ACTION_REQUEST_UNINSTALL_COLOR);
		label2textColor.put(KEY_ALWAYS, Globals.ACTION_REQUEST_ALWAYS_COLOR);
		label2textColor.put(KEY_ONCE, Globals.ACTION_REQUEST_ONCE_COLOR);
		label2textColor.put(KEY_CUSTOM, Globals.ACTION_REQUEST_CUSTOM_COLOR);
	}

	public static List<String> getScriptKeys() {
		checkCollections();
		return scriptKeys;
	}

	public static Map<String, String> getScriptKey2Label() {
		checkCollections();
		return scriptKey2label;
	}

	public static Map<Integer, String> getState2Label() {
		checkCollections();
		return state2label;
	}

	public static Map<String, Color> getLabel2TextColor() {
		checkCollections();

		return label2textColor;
	}

	public static String getLabel(int state) {
		checkCollections();

		return state2label.get(state);
	}

	public static List<String> getLabels() {
		checkCollections();

		return new ArrayList<>(labels).subList(2, labels.size());
	}

	public static Integer getVal(String label) {
		checkCollections();

		if (label == null || label.isEmpty()) {
			return NONE;
		}

		return label2state.get(label);
	}

	public static String[] getDisplayLabelsForChoice() {
		checkCollections();

		return choiceLabels;
	}

	// instance methods
	public int getVal() {
		return state;
	}

	@Override
	public String toString() {
		return getLabel(state);
	}

	public static String produceFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return getLabel(NONE);
		}

		if (!labels.contains(label)) {
			return getLabel(INVALID);
		}

		return label;

	}

	public static ActionRequest produceActionRequestFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return new ActionRequest(NONE);
		}

		if (!labels.contains(label)) {
			return new ActionRequest(INVALID);
		}

		return new ActionRequest(getVal(label));
	}
}
