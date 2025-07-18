package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public sealed interface HealthCheckSettingsMsg permits HealthCheckSettingsMsg.SimpleMsg,
		HealthCheckSettingsMsg.HostsSelected, HealthCheckSettingsMsg.CheckActiveChanged,
		HealthCheckSettingsMsg.StartDowntimeChanged, HealthCheckSettingsMsg.EndDowntimeChanged {
	enum SimpleMsg implements HealthCheckSettingsMsg {
		SAVE_CLICKED, CANCLE_CLICKED
	}

	record HostsSelected(List<String> hosts) implements HealthCheckSettingsMsg {
	}

	record CheckActiveChanged(FlatTriStateCheckBox.State state) implements HealthCheckSettingsMsg {
	}

	record StartDowntimeChanged(String value) implements HealthCheckSettingsMsg {
	}

	record EndDowntimeChanged(String value) implements HealthCheckSettingsMsg {
	}
}