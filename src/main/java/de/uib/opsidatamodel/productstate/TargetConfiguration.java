/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

public final class TargetConfiguration {
	public static final String KEY = "targetConfiguration";

	private static String[] labels;
	private static String[] choiceLabels;

	// private class to prevent instantiation
	private TargetConfiguration() {
	}

	private static void checkCollections() {
		if (labels != null) {
			return;
		}

		labels = new String[] { "undefined", InstallationStatus.KEY_INSTALLED, "always", "forbidden" };

		choiceLabels = new String[] { "undefined", InstallationStatus.KEY_INSTALLED, "always", "forbidden" };
	}

	public static String[] getLabels() {
		checkCollections();

		return labels;
	}

	public static String[] getDisplayLabelsForChoice() {
		checkCollections();

		return choiceLabels;
	}
}
