package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public sealed interface HealthCheckSettingsMsg
		permits HealthCheckSettingsMsg.HostsSelected, HealthCheckSettingsMsg.CheckActiveChanged,
		HealthCheckSettingsMsg.StartDowntimeChanged, HealthCheckSettingsMsg.EndDowntimeChanged,
		HealthCheckSettingsMsg.SaveClicked, HealthCheckSettingsMsg.CancelClicked {

	record HostsSelected(List<String> hosts) implements HealthCheckSettingsMsg {
	}

	record CheckActiveChanged(FlatTriStateCheckBox.State state) implements HealthCheckSettingsMsg {
	}

	record StartDowntimeChanged(String value) implements HealthCheckSettingsMsg {
	}

	record EndDowntimeChanged(String value) implements HealthCheckSettingsMsg {
	}

	final class SaveClicked implements HealthCheckSettingsMsg {}

	final class CancelClicked implements HealthCheckSettingsMsg {}
}