package de.uib.configed.gui.healthcheck.settings;

public sealed interface HealthCheckSettingsEffect permits HealthCheckSettingsEffect.SimpleEffect {
	enum SimpleEffect implements HealthCheckSettingsEffect {
		SAVE, CLOSE
	}
}
