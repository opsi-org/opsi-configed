package de.uib.configed.gui.healthcheck.settings;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent;

public class HealthCheckSettingsUpdate {

	private HealthCheckSettingsUpdate() {
		// Hide constructor.
	}

	public static AbstractTeaComponent.UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> update(
			HealthCheckSettingsModel model, HealthCheckSettingsMsg msg) {
		return switch (msg) {
		case HealthCheckSettingsMsg.HostsSelected(var hosts) -> new AbstractTeaComponent.UpdateResult<>(
				model.withSelectedHosts(hosts), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.CheckActiveChanged(var state) -> new AbstractTeaComponent.UpdateResult<>(
				model.withCheckActiveState(state).withSaveEnabled(state != FlatTriStateCheckBox.State.INDETERMINATE),
				new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.StartDowntimeChanged(var value) -> new AbstractTeaComponent.UpdateResult<>(
				model.withStartDowntime(value), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.EndDowntimeChanged(var value) -> new AbstractTeaComponent.UpdateResult<>(
				model.withEndDowntime(value), new HealthCheckSettingsEffect.None());
		case HealthCheckSettingsMsg.SaveClicked ignored -> new AbstractTeaComponent.UpdateResult<>(model,
				new HealthCheckSettingsEffect.Save());
		case HealthCheckSettingsMsg.CancelClicked ignored -> new AbstractTeaComponent.UpdateResult<>(model,
				new HealthCheckSettingsEffect.Close());
		};
	}
}