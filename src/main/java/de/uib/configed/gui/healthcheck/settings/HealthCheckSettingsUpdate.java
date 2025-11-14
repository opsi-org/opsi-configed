/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

public class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.HostsSelectionRequested() -> UpdateResult.withEffect(model,
				HealthCheckSettingsEffect.SimpleEffect.SELECT_HOSTS);
		case HealthCheckSettingsMsg.HostsSelected(List<String> hosts) -> UpdateResult
				.noEffect(model.withSelectedHosts(hosts));
		case HealthCheckSettingsMsg.ToggleActivity(FlatTriStateCheckBox.State state) -> UpdateResult.noEffect(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE));
		case HealthCheckSettingsMsg.DowntimeSelectionRequested(DowntimeType downtimeType) -> UpdateResult
				.withEffect(model, new HealthCheckSettingsEffect.SelectDownTime(downtimeType));
		case HealthCheckSettingsMsg.DowntimeSelected(DowntimeType downtimeType, String value) -> {
			UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = null;
			if (DowntimeType.START == downtimeType) {
				result = UpdateResult.noEffect(model.withStartDowntime(value));
			} else {
				result = UpdateResult.noEffect(model.withEndDowntime(value));
			}
			yield result;
		}
		case HealthCheckSettingsMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		};
	}

	private static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> handleSimpleMsg(
			HealthCheckSettingsMsg.SimpleMsg msg, HealthCheckSettingsModel model) {
		return switch (msg) {
		case SAVE -> UpdateResult.withEffect(model, HealthCheckSettingsEffect.SimpleEffect.SAVE);
		case CANCLE -> UpdateResult.withEffect(model, HealthCheckSettingsEffect.SimpleEffect.CLOSE);
		};
	}
}