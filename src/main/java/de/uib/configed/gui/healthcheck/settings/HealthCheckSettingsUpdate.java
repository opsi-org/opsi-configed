package de.uib.configed.gui.healthcheck.settings;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

public class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static AbstractTeaComponent.UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.HostsSelected(var hosts) -> UpdateResult.noEffect(model.withSelectedHosts(hosts));
		case HealthCheckSettingsMsg.CheckActiveChanged(var state) -> UpdateResult.noEffect(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE));
		case HealthCheckSettingsMsg.StartDowntimeChanged(var value) -> UpdateResult
				.noEffect(model.withStartDowntime(value));
		case HealthCheckSettingsMsg.EndDowntimeChanged(var value) -> UpdateResult
				.noEffect(model.withEndDowntime(value));
		case HealthCheckSettingsMsg.SaveClicked ignored -> UpdateResult.withEffect(model,
				new HealthCheckSettingsEffect.Save());
		case HealthCheckSettingsMsg.CancelClicked ignored -> UpdateResult.withEffect(model,
				new HealthCheckSettingsEffect.Close());
		};
	}
}