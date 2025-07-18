package de.uib.configed.gui.healthcheck.settings;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

public class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.SelectHosts(var hosts) -> UpdateResult.noEffect(model.withSelectedHosts(hosts));
		case HealthCheckSettingsMsg.ToggleActivity(var state) -> UpdateResult.noEffect(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE));
		case HealthCheckSettingsMsg.ChangeStartDowntime(var value) -> UpdateResult
				.noEffect(model.withStartDowntime(value));
		case HealthCheckSettingsMsg.ChangeEndDowntime(var value) -> UpdateResult.noEffect(model.withEndDowntime(value));
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