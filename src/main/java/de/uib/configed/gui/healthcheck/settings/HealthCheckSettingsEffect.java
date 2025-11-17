/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

public sealed interface HealthCheckSettingsEffect
		permits HealthCheckSettingsEffect.SimpleEffect, HealthCheckSettingsEffect.SelectDownTime {
	enum SimpleEffect implements HealthCheckSettingsEffect {
		SAVE_CONFIG, CLOSE_DIALOG, OPEN_HOST_SELECTION_DIALOG
	}

	record SelectDownTime(DowntimeType downtimeType) implements HealthCheckSettingsEffect {
	}
}
