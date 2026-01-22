/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

public sealed interface HealthCheckSettingsEffect
		permits HealthCheckSettingsEffect.SimpleEffect, HealthCheckSettingsEffect.SelectDownTime {
	enum SimpleEffect implements HealthCheckSettingsEffect {
		SAVE_SETTINGS, DISMISS_SETTINGS, OPEN_HOST_SELECTION_DIALOG
	}

	record SelectDownTime(DowntimeType downtimeType) implements HealthCheckSettingsEffect {
	}
}
