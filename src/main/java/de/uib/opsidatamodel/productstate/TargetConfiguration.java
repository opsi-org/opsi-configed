/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.uib.configed.Globals;

public class TargetConfiguration {
	public static final String KEY = "targetConfiguration";

	private static Set<String> labels;
	private static String[] choiceLabels;

	private static void checkCollections() {
		if (labels != null) {
			return;
		}

		labels = new LinkedHashSet<>();
		labels.add("undefined");
		labels.add(InstallationStatus.KEY_INSTALLED);
		labels.add("always");
		labels.add("forbidden");

		choiceLabels = new String[] { "undefined", InstallationStatus.KEY_INSTALLED, "always", "forbidden" };
	}

	public static List<String> getLabels() {
		checkCollections();

		return new ArrayList<>(labels);
	}

	public static final String[] getDisplayLabelsForChoice() {
		checkCollections();

		return choiceLabels;
	}

	// instance methods

	@Override
	public String toString() {
		return Globals.NO_VALID_STATE_STRING;
	}
}
