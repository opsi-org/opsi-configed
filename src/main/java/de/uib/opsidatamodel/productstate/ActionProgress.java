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

public class ActionProgress {
	public static final String KEY = "actionProgress";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// product offers no entry
	public static final int NOT_AVAILABLE = -6;

	// valid service states
	public static final int NONE = 0;
	public static final int INSTALLING = 1;
	public static final int CACHED = 2;

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> label2displayLabel;
	private static Map<String, String> displayLabel2label;

	private static List<Integer> states;
	private static List<String> labels;

	// instance variable
	private int state = INVALID;

	// constructor
	public ActionProgress() {
	}

	public ActionProgress(int t) {
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
		states.add(INSTALLING);
		states.add(CACHED);

		labels = new ArrayList<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("not_available");
		labels.add("none");
		labels.add("installing");
		labels.add("cached");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(INSTALLING, "installing");
		state2label.put(CACHED, "cached");

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("installing", INSTALLING);
		label2state.put("cached", CACHED);

		label2displayLabel = new HashMap<>();
		label2displayLabel.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		label2displayLabel.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		label2displayLabel.put("not_available", "not_available");
		label2displayLabel.put("none", "no process reported");

		label2displayLabel.put("installing", "installing");
		label2displayLabel.put("cached", "cached");

		displayLabel2label = new HashMap<>();
		displayLabel2label.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		displayLabel2label.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		displayLabel2label.put("not_available", "not_available");
		displayLabel2label.put("no process reported", "none");

		displayLabel2label.put("installing", "installing");
		displayLabel2label.put("cached", "cached");
	}

	public static Map<String, String> getLabel2DisplayLabel() {
		checkCollections();

		return label2displayLabel;
	}

	public static boolean existsState(int state) {
		checkCollections();

		return states.contains(state);
	}

	public static boolean existsLabel(String label) {
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

		return label2displayLabel.get(getLabel(state));
	}

	// instance methods

	public int getVal() {
		return state;
	}

	public String getString() {
		return getLabel(state);
	}

	@Override
	public String toString() {
		return getLabel(state);
	}

	// getting instances
	public static ActionProgress produceFromDisplayLabel(String display) {
		return produceFromLabel(displayLabel2label.get(display));
	}

	public static ActionProgress produceFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return new ActionProgress(NOT_AVAILABLE);
		}

		if (!labels.contains(label)) {
			return new ActionProgress(INVALID);
		}

		return new ActionProgress(getVal(label));
	}
}
