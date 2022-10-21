package de.uib.configed.gui;

/**
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2019 uib.de

 */
 
import de.uib.configed.*;
import de.uib.configed.gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.logging.*;
 
public class CombinedMenuItem
{
	JCheckBoxMenuItem standardMenuItem;
	JCheckBoxMenuItem popupItem;
	CombinedMenuItem( 
			JCheckBoxMenuItem menuItem,
			JCheckBoxMenuItem popupItem
			)
	{
		this.standardMenuItem = menuItem;
		this.popupItem = popupItem;
	}
	
	
	private ItemListener[] stopItemListeners( JMenuItem itemOwner )
	{
		ItemListener[] myItemListeners = itemOwner.getItemListeners();
		for (ItemListener l : myItemListeners)
		{
			itemOwner.removeItemListener( l );
		}
		return myItemListeners;
	}
	
	private void startItemListeners( JMenuItem itemOwner, ItemListener[] listeners )
	{
		if (listeners == null)
			return;
		
		for (ItemListener l : listeners)
		{
			itemOwner.addItemListener( l );
		}
	}

	
	public void show( boolean b )
	{
		ItemListener[] theListeners = stopItemListeners( standardMenuItem  );
		standardMenuItem.setState( b );
		startItemListeners( standardMenuItem, theListeners );
		
		theListeners = stopItemListeners(popupItem);
		popupItem.setState( b );
		startItemListeners( popupItem, theListeners ); 
	}
}