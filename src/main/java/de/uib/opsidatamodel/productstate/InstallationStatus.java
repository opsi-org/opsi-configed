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
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.Globals;

public final class InstallationStatus {
	public static final String KEY = "installationStatus";

	public static final String KEY_NOT_INSTALLED = "not_installed";
	public static final String KEY_INSTALLED = "installed";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// does not matter
	public static final int UNDEFINED = -1;

	// valid service states since 4.0
	public static final int NOT_INSTALLED = 0;
	public static final int INSTALLED = 1;
	public static final int UNKNOWN = 2;

	// compatibility mode for older opsi data, not more necessary

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, Color> label2textColor;

	private static Set<Integer> states;
	private static List<String> labels;
	private static String[] choiceLabels;

	// instance variable
	private int state = INVALID;

	private InstallationStatus(int t) {
		if (existsState(t)) {
			state = t;
		} else {
			state = INVALID;
		}
	}

	private static void checkCollections() {
		if (states != null) {
			return;
		}

		states = new HashSet<>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(UNDEFINED);
		states.add(INSTALLED);
		states.add(NOT_INSTALLED);

		states.add(UNKNOWN);

		labels = new ArrayList<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("undefined");
		labels.add(InstallationStatus.KEY_INSTALLED);
		labels.add(InstallationStatus.KEY_NOT_INSTALLED);

		labels.add("unknown");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(UNDEFINED, "undefined");
		state2label.put(INSTALLED, InstallationStatus.KEY_INSTALLED);
		state2label.put(NOT_INSTALLED, InstallationStatus.KEY_NOT_INSTALLED);

		state2label.put(UNKNOWN, "unknown");

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put("undefined", UNDEFINED);
		label2state.put(InstallationStatus.KEY_INSTALLED, INSTALLED);
		label2state.put(InstallationStatus.KEY_NOT_INSTALLED, NOT_INSTALLED);

		label2state.put("unknown", UNKNOWN);

		choiceLabels = new String[] { InstallationStatus.KEY_NOT_INSTALLED, InstallationStatus.KEY_INSTALLED,
				"unknown" };

		label2textColor = new HashMap<>();
		label2textColor.put(InstallationStatus.KEY_NOT_INSTALLED, Globals.INSTALLATION_STATUS_NOT_INSTALLED_COLOR);
		label2textColor.put(InstallationStatus.KEY_INSTALLED, Globals.INSTALLATION_STATUS_INSTALLED_COLOR);
		label2textColor.put("unknown", Globals.INSTALLATION_STATUS_UNKNOWN_COLOR);
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

		return labels;
	}

	public static Integer getVal(String label) {
		checkCollections();

		if (label == null || label.isEmpty()) {
			// action requests
			return UNKNOWN;
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

	public static InstallationStatus produceFromLabel(String label) {
		checkCollections();

		if (label == null) {
			return new InstallationStatus(INVALID);
		}

		if (!labels.contains(label)) {
			return new InstallationStatus(INVALID);
		}

		return new InstallationStatus(getVal(label));
	}
}
