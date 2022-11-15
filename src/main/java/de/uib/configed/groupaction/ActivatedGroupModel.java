/* 
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
 
package de.uib.configed.groupaction;

import java.util.*;
import javax.swing.tree.*;

import de.uib.configed.gui.*;
import de.uib.configed.tree.*;
import de.uib.utilities.tree.*;

import de.uib.configed.HostsStatusInfo;
import de.uib.configed.configed;
import de.uib.configed.Globals;

import de.uib.utilities.logging.*;


public class ActivatedGroupModel
{
	protected String groupName;
	protected String groupDescription;
	protected TreePath path;
	protected DefaultMutableTreeNode node;
	
	protected Set<String> associatedClients;
	
	protected boolean active = false;
	
	private HostsStatusInfo hostsInfo;
	
	public ActivatedGroupModel(HostsStatusInfo hostsInfo)
	{
		this.hostsInfo = hostsInfo;
	}
	
	public void setActive(boolean b)
	{
		logging.info(this, "setActive " + b);
		
		active = b;
		
		if (b && groupName != null)
		{
			hostsInfo.setGroupName(groupName);
			hostsInfo.setGroupClientsCount( associatedClients.size() );
			configed.savedStates.saveGroupSelection.serialize(groupName);
		}
		
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setNode(String name, DefaultMutableTreeNode n, TreePath p)
	{
		logging.info(this, "setNode " + name);
		groupName = name;
		node = n;
		path = p;
		hostsInfo.setGroupName(name);
	}
	
	public void setDescription(String s)
	{
		groupDescription = s;
	}
	
	public void setAssociatedClients (Set<String> clients)
	{
		associatedClients = clients;
		hostsInfo.setGroupClientsCount( clients.size() );
	}
	
	public Set<String> getAssociatedClients()
	{
		return associatedClients;
	}
		
	
	public int getNumberOfClients()
	{
		if (associatedClients == null)
			return 0;
		
		return associatedClients.size();
	}
	
	public String getGroupName()
	{
		return "" + groupName;
	}
	
	public String getLabel()
	{
		if (
			groupName != null 
			&& groupDescription != null 
			&& !groupDescription.equals(groupName)
			&& !groupDescription.equals("")
			)
			return 
			//"<html><b>" + 
			groupName 
			//+ "</b> "
			+  "  (" + groupDescription + ") "
			//+ </html>"
			;
		
		return 
			""
			//+ <html><b>" 
			+ groupName 
			//+ "</b></html>"
			;
	}
	
	public String getGroupDescription()
	{
		return "" + groupDescription;
	}
	
}
	
	
	
	
	
	
	