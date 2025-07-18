package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public sealed interface HealthCheckSettingsMsg permits HealthCheckSettingsMsg.SimpleMsg,
		HealthCheckSettingsMsg.SelectHosts, HealthCheckSettingsMsg.ToggleActivity,
		HealthCheckSettingsMsg.ChangeStartDowntime, HealthCheckSettingsMsg.ChangeEndDowntime {
	enum SimpleMsg implements HealthCheckSettingsMsg {
		SAVE, CANCLE
	}

	record SelectHosts(List<String> hosts) implements HealthCheckSettingsMsg {
	}

	record ToggleActivity(FlatTriStateCheckBox.State state) implements HealthCheckSettingsMsg {
	}

	record ChangeStartDowntime(String value) implements HealthCheckSettingsMsg {
	}

	record ChangeEndDowntime(String value) implements HealthCheckSettingsMsg {
	}
}