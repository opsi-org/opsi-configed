package de.uib.configed.gui;

import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

public class CombinedMenuItem {
	JCheckBoxMenuItem standardMenuItem;
	JCheckBoxMenuItem popupItem;

	CombinedMenuItem(JCheckBoxMenuItem menuItem, JCheckBoxMenuItem popupItem) {
		this.standardMenuItem = menuItem;
		this.popupItem = popupItem;
	}

	private static ItemListener[] stopItemListeners(JMenuItem itemOwner) {
		ItemListener[] myItemListeners = itemOwner.getItemListeners();
		for (ItemListener l : myItemListeners) {
			itemOwner.removeItemListener(l);
		}
		return myItemListeners;
	}

	private static void startItemListeners(JMenuItem itemOwner, ItemListener[] listeners) {
		if (listeners == null) {
			return;
		}

		for (ItemListener l : listeners) {
			itemOwner.addItemListener(l);
		}
	}

	public void show(boolean b) {
		ItemListener[] theListeners = stopItemListeners(standardMenuItem);
		standardMenuItem.setState(b);
		startItemListeners(standardMenuItem, theListeners);

		theListeners = stopItemListeners(popupItem);
		popupItem.setState(b);
		startItemListeners(popupItem, theListeners);
	}
}
