package de.uib.configed.gui.healthcheck.settings;

public sealed interface HealthCheckSettingsEffect
		permits HealthCheckSettingsEffect.Save, HealthCheckSettingsEffect.Close, HealthCheckSettingsEffect.None {

	final class Save implements HealthCheckSettingsEffect {}

	final class Close implements HealthCheckSettingsEffect {}

	final class None implements HealthCheckSettingsEffect {}
}
