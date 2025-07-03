package de.uib.configed.gui.healthcheck.settings;

import java.util.List;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public class HealthCheckSettingsModel {
	private final List<String> selectedHosts;
	private final FlatTriStateCheckBox.State checkActiveState;
	private final String startDowntime;
	private final String endDowntime;
	private final boolean saveEnabled;

	public HealthCheckSettingsModel(List<String> selectedHosts, FlatTriStateCheckBox.State checkActiveState,
			String startDowntime, String endDowntime, boolean saveEnabled) {
		this.selectedHosts = selectedHosts;
		this.checkActiveState = checkActiveState;
		this.startDowntime = startDowntime;
		this.endDowntime = endDowntime;
		this.saveEnabled = saveEnabled;
	}

	public List<String> getSelectedHosts() {
		return selectedHosts;
	}

	public FlatTriStateCheckBox.State getCheckActiveState() {
		return checkActiveState;
	}

	public String getStartDowntime() {
		return startDowntime;
	}

	public String getEndDowntime() {
		return endDowntime;
	}

	public boolean isSaveEnabled() {
		return saveEnabled;
	}

	public HealthCheckSettingsModel withSelectedHosts(List<String> selectedHosts) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}

	public HealthCheckSettingsModel withCheckActiveState(FlatTriStateCheckBox.State checkActiveState) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}

	public HealthCheckSettingsModel withStartDowntime(String startDowntime) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}

	public HealthCheckSettingsModel withEndDowntime(String endDowntime) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}

	public HealthCheckSettingsModel withSaveEnabled(boolean saveEnabled) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}

	public static HealthCheckSettingsModel initial(List<String> selectedHosts,
			FlatTriStateCheckBox.State checkActiveState, String startDowntime, String endDowntime,
			boolean saveEnabled) {
		return new HealthCheckSettingsModel(selectedHosts, checkActiveState, startDowntime, endDowntime, saveEnabled);
	}
}