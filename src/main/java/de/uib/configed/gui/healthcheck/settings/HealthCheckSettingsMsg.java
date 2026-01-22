/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public sealed interface HealthCheckSettingsMsg
		permits HealthCheckSettingsMsg.SimpleMsg, HealthCheckSettingsMsg.HostsSelectionRequested,
		HealthCheckSettingsMsg.HostsSelected, HealthCheckSettingsMsg.ToggleActivity,
		HealthCheckSettingsMsg.DowntimeSelectionRequested, HealthCheckSettingsMsg.DowntimeSelected {
	enum SimpleMsg implements HealthCheckSettingsMsg {
		SAVE_SETTINGS, CANCLE_SETTINGS
	}

	record HostsSelectionRequested() implements HealthCheckSettingsMsg {
	}

	record HostsSelected(List<String> hosts) implements HealthCheckSettingsMsg {
	}

	record ToggleActivity(FlatTriStateCheckBox.State state) implements HealthCheckSettingsMsg {
	}

	record DowntimeSelectionRequested(DowntimeType downtimeType) implements HealthCheckSettingsMsg {
	}

	record DowntimeSelected(DowntimeType downtimeType, String value) implements HealthCheckSettingsMsg {
	}
}
