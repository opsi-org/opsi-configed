/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	private static Set<Integer> states;
	private static Set<String> labels;
	private static String[] choiceLabels;
	private static List<String> scriptKeys;

	private static Map<String, String> scriptKey2label;

	// instance variable
	private int state = INVALID;

	public ActionRequest(int t) {
		if (existsState(t)) {
			state = t;
		} else {
			state = NOT_AVAILABLE;
		}
	}

	private static void checkCollections() {
		if (states != null) {
			return;
		}

		states = new HashSet<>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(NOT_AVAILABLE);
		states.add(NONE);
		states.add(SETUP);
		states.add(UPDATE);
		states.add(UNINSTALL);
		states.add(ALWAYS);
		states.add(ONCE);
		states.add(CUSTOM);

		labels = new LinkedHashSet<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("not_available");
		labels.add("none");
		labels.add("setup");
		labels.add("update");
		labels.add("uninstall");
		labels.add("always");
		labels.add("once");
		labels.add("custom");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(SETUP, "setup");
		state2label.put(UPDATE, "update");
		state2label.put(UNINSTALL, "uninstall");
		state2label.put(ALWAYS, "always");
		state2label.put(ONCE, "once");
		state2label.put(CUSTOM, "custom");

		Map<String, Integer> serviceValue2state = new HashMap<>();
		serviceValue2state.put("setup", SETUP);
		serviceValue2state.put("update", UPDATE);
		serviceValue2state.put("uninstall", UNINSTALL);
		serviceValue2state.put("always", ALWAYS);
		serviceValue2state.put("once", ONCE);
		serviceValue2state.put("custom", CUSTOM);

		scriptKeys = new ArrayList<>();

		scriptKey2label = new HashMap<>();

		for (Entry<String, Integer> stateEntry : serviceValue2state.entrySet()) {
			scriptKeys.add(stateEntry.getKey() + "Script");

			scriptKey2label.put(stateEntry.getKey() + "Script", state2label.get(stateEntry.getValue()));
		}

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("setup", SETUP);
		label2state.put("update", UPDATE);
		label2state.put("uninstall", UNINSTALL);
		label2state.put("always", ALWAYS);
		label2state.put("once", ONCE);
		label2state.put("custom", CUSTOM);

		choiceLabels = new String[] { "none", "setup", "update", "uninstall", "always", "once", "custom", };

		label2textColor = new HashMap<>();
		label2textColor.put("none", Globals.ACTION_REQUEST_NONE_COLOR);
		label2textColor.put("setup", Globals.ACTION_REQUEST_SETUP_COLOR);
		label2textColor.put("update", Globals.ACTION_REQUEST_UPDATE_COLOR);
		label2textColor.put("uninstall", Globals.ACTION_REQUEST_UNINSTALL_COLOR);
		label2textColor.put("always", Globals.ACTION_REQUEST_ALWAYS_COLOR);
		label2textColor.put("once", Globals.ACTION_REQUEST_ONCE_COLOR);
		label2textColor.put("custom", Globals.ACTION_REQUEST_CUSTOM_COLOR);
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

	private static boolean existsState(int state) {
		checkCollections();

		return states.contains(state);
	}

	private static boolean existsLabel(String label) {
		checkCollections();

		return labels.contains(label);
	}

	public static String getLabel(int state) {
		checkCollections();

		if (!existsState(state)) {
			return null;
		}

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

		if (!existsLabel(label)) {
			return null;
		}

		return label2state.get(label);
	}

	public static String getDisplayLabel(int state) {
		checkCollections();

		return getLabel(state);
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

	public static ActionRequest produceFromLabel(String label) {
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
