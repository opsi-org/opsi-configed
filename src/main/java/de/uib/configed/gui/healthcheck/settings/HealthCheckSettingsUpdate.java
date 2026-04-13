/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

public final class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.HostsSelectionRequested() -> UpdateResult.withEffect(model,
				HealthCheckSettingsEffect.SimpleEffect.OPEN_HOST_SELECTION_DIALOG);
		case HealthCheckSettingsMsg.HostsSelected(List<String> hosts) -> UpdateResult
				.noEffect(model.withSelectedHosts(hosts));
		case HealthCheckSettingsMsg.ToggleActivity(FlatTriStateCheckBox.State state) -> UpdateResult.noEffect(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE));
		case HealthCheckSettingsMsg.DowntimeSelectionRequested(DowntimeType downtimeType) -> UpdateResult
				.withEffect(model, new HealthCheckSettingsEffect.SelectDownTime(downtimeType));
		case HealthCheckSettingsMsg.DowntimeSelected(DowntimeType downtimeType, String value) -> handleDownTimeSelectedMsg(
				model, downtimeType, value);
		case HealthCheckSettingsMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		};
	}

	private static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> handleDownTimeSelectedMsg(
			HealthCheckSettingsModel model, DowntimeType downtimeType, String value) {
		return DowntimeType.START == downtimeType ? UpdateResult.noEffect(model.withStartDowntime(value))
				: UpdateResult.noEffect(model.withEndDowntime(value));
	}

	private static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> handleSimpleMsg(
			HealthCheckSettingsMsg.SimpleMsg msg, HealthCheckSettingsModel model) {
		return switch (msg) {
		case SAVE_SETTINGS -> UpdateResult.withEffect(model, HealthCheckSettingsEffect.SimpleEffect.SAVE_SETTINGS);
		case CANCLE_SETTINGS -> UpdateResult.withEffect(model, HealthCheckSettingsEffect.SimpleEffect.DISMISS_SETTINGS);
		};
	}
}
