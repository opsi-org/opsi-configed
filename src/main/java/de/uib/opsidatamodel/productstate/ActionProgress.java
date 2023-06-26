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
	private static Map<String, String> label2displayLabel;

	private static List<Integer> states;

	// instance variable
	private int state = INVALID;

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

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(INSTALLING, "installing");
		state2label.put(CACHED, "cached");

		label2displayLabel = new HashMap<>();
		label2displayLabel.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		label2displayLabel.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		label2displayLabel.put("not_available", "not_available");
		label2displayLabel.put("none", "no process reported");

		label2displayLabel.put("installing", "installing");
		label2displayLabel.put("cached", "cached");
	}

	public static Map<String, String> getLabel2DisplayLabel() {
		checkCollections();

		return label2displayLabel;
	}

	private static boolean existsState(int state) {
		checkCollections();

		return states.contains(state);
	}

	private static String getLabel(int state) {
		checkCollections();

		if (!existsState(state)) {
			return null;
		}

		return state2label.get(state);
	}

	public static String getDisplayLabel(int state) {
		checkCollections();

		return label2displayLabel.get(getLabel(state));
	}

	@Override
	public String toString() {
		return getLabel(state);
	}
}
