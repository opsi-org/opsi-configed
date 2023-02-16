package de.uib.utilities.swing.tabbedpane;

import de.uib.configed.ConfigedMain.LicencesTabStatus;

public interface TabController {
	public abstract LicencesTabStatus getStartTabState();

	public abstract LicencesTabStatus reactToStateChangeRequest(LicencesTabStatus newState);

	public void addClient(LicencesTabStatus state, TabClient client);

	public TabClient getClient(LicencesTabStatus state);
}
