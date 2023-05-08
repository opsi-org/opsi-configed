package de.uib.configed;

public interface HostsStatusInfo {
	void setGroupName(String s);

	void setGroupClientsCount(int n);

	void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots);

	String getSelectedClientNames();

	String getInvolvedDepots();

	String getGroupName();
}
