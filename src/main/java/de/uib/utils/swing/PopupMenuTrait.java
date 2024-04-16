/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.uib.configed.Configed;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class PopupMenuTrait extends JPopupMenu {
	public static final int POPUP_SEPARATOR = 0;
	public static final int POPUP_RELOAD = 4;

	public static final int POPUP_SAVE = 8;
	public static final int POPUP_SAVE_AS_ZIP = 9;

	public static final int POPUP_SAVE_ALL_AS_ZIP = 11;

	public static final int POPUP_DELETE = 13;
	public static final int POPUP_ADD = 14;

	public static final int POPUP_FLOATINGCOPY = 20;

	public static final int POPUP_PDF = 21;

	public static final int POPUP_EXPORT_CSV = 23;
	public static final int POPUP_EXPORT_SELECTED_CSV = 24;

	public static final int POPUP_PRINT = 30;

	private List<Integer> listPopups;

	private JMenuItem[] menuItems;

	public PopupMenuTrait(Integer[] popups) {
		listPopups = Arrays.asList(popups);

		menuItems = new JMenuItem[popups.length];

		for (Integer popup : popups) {
			addPopup(popup);
		}
	}

	private void addPopup(final int p) {
		int i;
		switch (p) {
		case POPUP_RELOAD:
			i = listPopups.indexOf(POPUP_RELOAD);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.reload"),
					Utils.createImageIcon("images/reload16.png", ""));

			// not work

			addItem(p);

			break;

		case POPUP_FLOATINGCOPY:
			addPopupFloatingCopy();

			break;

		case POPUP_SAVE:
			i = listPopups.indexOf(POPUP_SAVE);

			menuItems[i] = new JMenuItem(Configed.getResourceValue("save"), Utils.getSaveIcon());

			// not work

			addItem(p);

			break;

		case POPUP_SAVE_AS_ZIP:
			i = listPopups.indexOf(POPUP_SAVE_AS_ZIP);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.saveAsZip"),
					Utils.createImageIcon("images/zip-icon.png", ""));

			addItem(p);

			break;

		case POPUP_SAVE_ALL_AS_ZIP:
			i = listPopups.indexOf(POPUP_SAVE_ALL_AS_ZIP);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.saveAllAsZip"),
					Utils.createImageIcon("images/zip-icon.png", ""));

			addItem(p);

			break;

		case POPUP_PDF:
			i = listPopups.indexOf(POPUP_PDF);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"),
					Utils.createImageIcon("images/acrobat_reader16.png", ""));

			addItem(p);

			break;

		case POPUP_EXPORT_CSV:
			i = listPopups.indexOf(POPUP_EXPORT_CSV);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportTableAsCSV"));

			addItem(p);

			break;

		case POPUP_EXPORT_SELECTED_CSV:
			i = listPopups.indexOf(POPUP_EXPORT_SELECTED_CSV);
			menuItems[i] = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV"));

			addItem(p);

			break;

		case POPUP_DELETE:
			i = listPopups.indexOf(POPUP_DELETE);
			menuItems[i] = new JMenuItem("delete", Utils.createImageIcon("images/edit-delete.png", ""));

			addItem(p);

			break;

		case POPUP_ADD:
			i = listPopups.indexOf(POPUP_ADD);
			menuItems[i] = new JMenuItem("add", Utils.createImageIcon("images/list-add.png", ""));

			addItem(p);

			break;

		default:
			Logging.info(this, "popuptype " + p + " not implemented");
			break;
		}
	}

	private void addPopupFloatingCopy() {
		int i = listPopups.indexOf(POPUP_FLOATINGCOPY);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.floatingInstance"),
				Utils.createImageIcon("images/edit-copy.png", ""));

		// not work

		addSeparator();
		addItem(POPUP_FLOATINGCOPY);
	}

	public void setText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			Logging.info(this, "setText - popup " + popup + " not in list");
			return;
		}

		menuItems[i].setText(s);
	}

	public void setToolTipText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			Logging.info(this, "setToolTipText - popup " + popup + " not in list");
			return;
		}

		menuItems[i].setToolTipText(s);
	}

	private void addItem(final int p) {
		int i = listPopups.indexOf(p);
		menuItems[i].addActionListener(actionEvent -> action(p));

		add(menuItems[i]);
	}

	public void addPopupListenersTo(JComponent[] components) {
		for (JComponent component : components) {
			component.addMouseListener(new PopupMouseListener(this));
		}
	}

	// should be overwritten for specific actions in subclasses
	public void action(int p) {
		Logging.debug(this, "action called for type " + p);
	}
}
