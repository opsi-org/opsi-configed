/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;

public class LastAction {
	public static final String KEY = "lastAction";

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

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> displayLabel2label;

	private static List<Integer> states;
	private static List<String> labels;

	// instance variable
	private int state = INVALID;

	private LastAction(int t) {
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

		states = new ArrayList<>();
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

		labels = new ArrayList<>();
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

		displayLabel2label = new HashMap<>();
		displayLabel2label.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		displayLabel2label.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		displayLabel2label.put("not_available", "not_available");
		displayLabel2label.put("none", "none");
		displayLabel2label.put("setup", "setup");
		displayLabel2label.put("update", "update");
		displayLabel2label.put("uninstall", "uninstall");
		displayLabel2label.put("always", "always");
		displayLabel2label.put("once", "once");
		displayLabel2label.put("custom", "custom");
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

		return labels;
	}

	private static Integer getVal(String label) {
		checkCollections();

		if (label == null || label.isEmpty()) {
			return NONE;
		}

		if (!existsLabel(label)) {
			return null;
		}

		return label2state.get(label);
	}

	// instance methods

	public int getVal() {
		return state;
	}

	@Override
	public String toString() {
		return getLabel(state);
	}

	public static LastAction produceFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return new LastAction(NOT_AVAILABLE);
		}

		if (!labels.contains(label)) {
			return new LastAction(INVALID);
		}

		return new LastAction(getVal(label));
	}
}
