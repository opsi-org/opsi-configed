package de.uib.configed.gui.healthcheck.settings;

public sealed interface HealthCheckSettingsEffect
		permits HealthCheckSettingsEffect.SimpleEffect, HealthCheckSettingsEffect.SelectDownTime {
	enum SimpleEffect implements HealthCheckSettingsEffect {
		SAVE, CLOSE, SELECT_HOSTS
	}

	record SelectDownTime(DowntimeType downtimeType) implements HealthCheckSettingsEffect {
	}
}
