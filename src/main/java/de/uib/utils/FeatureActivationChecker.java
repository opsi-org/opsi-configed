/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;

import de.uib.utils.logging.Logging;

/**
 * The {@code FeatureActivationChecker} class provides methods to check the
 * activation status of features within an application. It allows checking if a
 * feature is activated, and retrieving the available features.
 * <p>
 * This class is designed for scenarios where clients need to test currently in
 * development features without making them available to all clients. Features
 * can be activated for testing purposes by including them in the {@code -ff} or
 * {@code --feature-flags} command option. Once activated, features cannot be
 * deactivated during runtime.
 * </p>
 * <p>
 * The available features are retrieved from the {@code Feature} enum, which
 * should contain all features available in the application. Developers can
 * include new features by adding enum constants to the {@code Feature} enum. An
 * empty enum signifies that no features are currently in development.
 * </p>
 */
public final class FeatureActivationChecker {
	public enum Feature {
	}

	private static EnumSet<Feature> activatedFeatures;

	private FeatureActivationChecker() {
	}

	/**
	 * Sets the activated features for the application.
	 *
	 * @param activatedFeatures a set containing the names of activated features
	 */
	public static void setActivatedFeatures(String[] activatedFeatures) {
		FeatureActivationChecker.activatedFeatures = convertToEnumSet(activatedFeatures);
	}

	private static EnumSet<Feature> convertToEnumSet(String[] featureNames) {
		EnumSet<Feature> enumSet = EnumSet.noneOf(Feature.class);
		for (String featureName : featureNames) {
			try {
				Feature feature = Feature.valueOf(featureName.toUpperCase(Locale.ROOT));
				enumSet.add(feature);
			} catch (IllegalArgumentException e) {
				Logging.warning("Invalid feature name: " + featureName);
			}
		}
		return enumSet;
	}

	/**
	 * Retrieves a string representation of available features.
	 * <p>
	 * This method returns a comma-separated string containing the names of
	 * available features defined in the {@code Feature} enum. The returned
	 * string does not include square brackets.
	 * </p>
	 *
	 * @return a string representing the names of available features
	 */
	public static String getAvailableFeaturesAsString() {
		String features = Arrays.toString(Feature.values());
		return features.substring(1, features.length() - 1);
	}

	/**
	 * Checks if any feature is available.
	 *
	 * @return {@code true} if there are available features, {@code false}
	 *         otherwise
	 */
	public static boolean hasAvailableFeatures() {
		return Feature.values().length != 0;
	}

	/**
	 * Checks if the specified feature is activated.
	 *
	 * @param feature the name of the feature to check
	 * @return {@code true} if the feature is activated, {@code false} otherwise
	 */
	public static boolean isFeatureActivated(Feature feature) {
		return activatedFeatures != null && activatedFeatures.contains(feature);
	}
}
