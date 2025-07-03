package de.uib.configed.gui.healthcheck.settings;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.TeaComponent;

public class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static TeaComponent.UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.HostsSelected(var hosts) -> new TeaComponent.UpdateResult<>(
				model.withSelectedHosts(hosts), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.CheckActiveChanged(var state) -> new TeaComponent.UpdateResult<>(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE),
				new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.StartDowntimeChanged(var value) -> new TeaComponent.UpdateResult<>(
				model.withStartDowntime(value), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.EndDowntimeChanged(var value) -> new TeaComponent.UpdateResult<>(
				model.withEndDowntime(value), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.SaveClicked ignored -> new TeaComponent.UpdateResult<>(model,
				new HealthCheckSettingsEffect.Save());
		case HealthCheckSettingsMsg.CancelClicked ignored -> new TeaComponent.UpdateResult<>(model,
				new HealthCheckSettingsEffect.Close());
		};
	}
}