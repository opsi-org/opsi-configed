/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.Globals;

public class TargetConfiguration {
	public static final String KEY = "targetConfiguration";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// valid service states
	public static final int UNDEFINED = 0;
	public static final int INSTALLED = 1;
	public static final int ALWAYS = 2;
	public static final int FORBIDDEN = 4;

	public static final int DISABLED = 5;
	public static final int INSTALL_NEWEST = 6;

	// mappings
	private static Map<Integer, String> state2label;

	private static Set<Integer> states;
	private static Set<String> labels;
	private static String[] choiceLabels;

	// instance variable
	private int state = INVALID;

	private static void checkCollections() {
		if (states != null) {
			return;
		}

		states = new HashSet<>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(UNDEFINED);
		states.add(INSTALLED);
		states.add(ALWAYS);
		states.add(FORBIDDEN);

		labels = new LinkedHashSet<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("undefined");
		labels.add(InstallationStatus.KEY_INSTALLED);
		labels.add("always");
		labels.add("forbidden");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(UNDEFINED, "undefined");
		state2label.put(INSTALLED, InstallationStatus.KEY_INSTALLED);
		state2label.put(ALWAYS, "always");
		state2label.put(FORBIDDEN, "forbidden");

		choiceLabels = new String[] { "undefined", InstallationStatus.KEY_INSTALLED, "always", "forbidden" };
	}

	private static boolean existsState(int state) {
		checkCollections();

		return states.contains(state);
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

	public static final String[] getDisplayLabelsForChoice() {
		checkCollections();

		return choiceLabels;
	}

	// instance methods

	@Override
	public String toString() {
		return getLabel(state);
	}
}
