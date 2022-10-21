package de.uib.utilities.swing.tabbedpane;

public interface TabController
{
	public abstract Enum getStartTabState();
	
	public abstract Enum reactToStateChangeRequest(Enum newState );
	
	public abstract boolean exit();
	
	public void addClient(Enum state, TabClient client);
	
	public void removeClient(Enum state);
	
	public TabClient getClient(Enum state);
	
}
