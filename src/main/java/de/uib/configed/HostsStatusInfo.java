package de.uib.configed;

public interface HostsStatusInfo {
	public void setGroupName(String s);

	public void setGroupClientsCount(int n);

	public void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots);

	public String getSelectedClientNames();

	public String getInvolvedDepots();

	public String getGroupName();

}
