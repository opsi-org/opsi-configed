/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.Globals;

public final class ActionResult {
	public static final String KEY = "actionResult";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// product offers no entry
	public static final int NOT_AVAILABLE = -6;

	// valid service states
	public static final int NONE = 0;
	public static final int FAILED = 2;
	public static final int SUCCESSFUL = 4;

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;

	private static Set<Integer> states;
	private static List<String> labels;

	// instance variable
	private int state = INVALID;

	private ActionResult(int t) {
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
		states.add(FAILED);
		states.add(SUCCESSFUL);

		labels = new ArrayList<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("not_available");
		labels.add("none");
		labels.add("failed");
		labels.add("successful");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(FAILED, "failed");
		state2label.put(SUCCESSFUL, "successful");

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("failed", FAILED);
		label2state.put("successful", SUCCESSFUL);
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

	public static String getDisplayLabel(int state) {
		checkCollections();

		return getLabel(state);
	}

	// instance methods

	public int getVal() {
		return state;
	}

	@Override
	public String toString() {
		return getLabel(state);
	}

	public static ActionResult produceFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return new ActionResult(NOT_AVAILABLE);
		}

		if (!labels.contains(label)) {
			return new ActionResult(INVALID);
		}

		return new ActionResult(getVal(label));
	}
}
