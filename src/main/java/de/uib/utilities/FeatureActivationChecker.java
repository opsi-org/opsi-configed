/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.util.HashSet;
import java.util.Set;

import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * The {@code FeatureActivationChecker} class provides methods to check the
 * activation status of features within an application. It allows checking if a
 * feature is available or activated, and retrieving the available features.
 * <p>
 * This class is designed for scenarios where clients need to test currently in
 * development features without making them available to all clients. Features
 * can be activated for testing purposes by including them in the {@code -ff} or
 * {@code --feature-flags} configuration option. Once activated, features cannot
 * be deactivated during runtime.
 * </p>
 * <p>
 * To indicate which features are currently in development and available for
 * testing, developers should include the feature names in the
 * {@code AVAILABLE_FEATURES} set. An empty set signifies that no features are
 * currently in development.
 * </p>
 */
public final class FeatureActivationChecker {
	private static final Set<String> AVAILABLE_FEATURES = Set.of("");
	private static Set<String> activatedFeatures = new HashSet<>();

	private FeatureActivationChecker() {
	}

	/**
	 * Sets the activated features for the application.
	 *
	 * @param activatedFeatures a set containing the names of activated features
	 */
	public static void setActivatedFeatures(Set<String> activatedFeatures) {
		Set<String> nonExistentFeatures = new HashSet<>(activatedFeatures);
		if (nonExistentFeatures.removeAll(AVAILABLE_FEATURES) || !AVAILABLE_FEATURES.containsAll(nonExistentFeatures)) {
			Logging.info("Following features were included, but are unavailable: "
					+ Utils.getCollectionStringRepresentation(nonExistentFeatures));
		}
		FeatureActivationChecker.activatedFeatures = activatedFeatures;
	}

	/**
	 * Retrieves the set of available features.
	 *
	 * @return a set containing the names of available features
	 */
	public static Set<String> getAvailableFeatures() {
		return AVAILABLE_FEATURES;
	}

	/**
	 * Checks if any feature is available.
	 *
	 * @return {@code true} if there are available features, {@code false}
	 *         otherwise
	 */
	public static boolean hasAvailableFeatures() {
		return !AVAILABLE_FEATURES.contains("");
	}

	/**
	 * Checks if the specified feature is activated.
	 *
	 * @param feature the name of the feature to check
	 * @return {@code true} if the feature is activated, {@code false} otherwise
	 */
	public static boolean isFeatureActivated(String feature) {
		return activatedFeatures.contains(feature);
	}
}
