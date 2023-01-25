package de.uib.utilities.swing.tabbedpane;

import de.uib.configed.ConfigedMain.LicencesTabStatus;

public interface TabController {
	public abstract Enum<LicencesTabStatus> getStartTabState();

	public abstract Enum reactToStateChangeRequest(Enum newState);

	public void addClient(Enum state, TabClient client);

	public TabClient getClient(Enum<LicencesTabStatus> state);
}
