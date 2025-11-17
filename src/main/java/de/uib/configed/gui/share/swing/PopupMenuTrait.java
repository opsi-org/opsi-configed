/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class PopupMenuTrait extends JPopupMenu {
	public static final int POPUP_SEPARATOR = 0;
	public static final int POPUP_RELOAD = 4;

	public static final int POPUP_SAVE = 8;
	public static final int POPUP_DOWNLOAD = 9;
	public static final int POPUP_DOWNLOAD_AS_ZIP = 10;

	public static final int POPUP_DOWNLOAD_ALL_AS_ZIP = 11;
	public static final int POPUP_COPY = 12;

	public static final int POPUP_DELETE = 13;
	public static final int POPUP_ADD = 14;

	public static final int POPUP_FLOATING_COPY = 20;

	public static final int POPUP_PDF = 21;

	public static final int POPUP_EXPORT_CSV = 23;
	public static final int POPUP_EXPORT_SELECTED_CSV = 24;

	public static final int POPUP_PRINT = 30;

	// map of popup type to method to create the popup item depending on the type
	private Map<Integer, Consumer<Integer>> popupCreators = Map.ofEntries(Map.entry(POPUP_RELOAD, this::addItemReload),
			Map.entry(POPUP_FLOATING_COPY, p -> addPopupFloatingCopy()), Map.entry(POPUP_SAVE, this::addItemSave),
			Map.entry(POPUP_COPY, this::addItemCopyText), Map.entry(POPUP_DOWNLOAD, this::addItemDownload),
			Map.entry(POPUP_DOWNLOAD_AS_ZIP, this::addItemDownloadAsZIP),
			Map.entry(POPUP_DOWNLOAD_ALL_AS_ZIP, this::addItemDownloadAllAsZIP), Map.entry(POPUP_PDF, this::addItemPDF),
			Map.entry(POPUP_EXPORT_CSV, this::addItemExportCSV),
			Map.entry(POPUP_EXPORT_SELECTED_CSV, this::addItemExportSelectedCSV),
			Map.entry(POPUP_DELETE, this::addItemDelete), Map.entry(POPUP_ADD, this::addItemAdd));

	private List<Integer> listPopups;

	private JMenuItem[] menuItems;

	private JComponent[] components;

	public PopupMenuTrait(Integer[] popups, Predicate<MouseEvent> condition, JComponent[] components) {
		listPopups = Arrays.asList(popups);
		this.components = components;
		menuItems = new JMenuItem[popups.length];

		for (Integer popup : popups) {
			addPopup(popup);
		}

		PopupMouseListener.addPopupMouseListenerToComponents(this, condition, components);
	}

	public PopupMenuTrait(Integer[] popups, JComponent[] components) {
		this(popups, null, components);
	}

	private void addPopup(final int p) {
		if (popupCreators.containsKey(p)) {
			popupCreators.get(p).accept(p);
		} else {
			Logging.info(this, "popuptype ", p, " not implemented");
		}
	}

	private void addItemReload(int p) {
		int i = listPopups.indexOf(POPUP_RELOAD);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("reload"));
		menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(menuItems[i], "refresh");

		if (components != null) {
			for (JComponent component : components) {
				Utils.addKeyBindingToJComponent(component, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), () -> action(p));
			}
		}

		// not work
		addItem(p);
	}

	private void addPopupFloatingCopy() {
		int i = listPopups.indexOf(POPUP_FLOATING_COPY);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.floatingInstance"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "copy");

		addSeparator();
		addItem(POPUP_FLOATING_COPY);
	}

	private void addItemSave(int p) {
		int i = listPopups.indexOf(POPUP_SAVE);

		menuItems[i] = new JMenuItem(Configed.getResourceValue("save"));
		menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(menuItems[i], "save");
		menuItems[i].setEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		addItem(p);
	}

	private void addItemDownload(int p) {
		int i = listPopups.indexOf(POPUP_DOWNLOAD);

		menuItems[i] = new JMenuItem(Configed.getResourceValue("download"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "download");

		addItem(p);
	}

	private void addItemDownloadAsZIP(int p) {
		int i = listPopups.indexOf(POPUP_DOWNLOAD_AS_ZIP);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.downloadAsZip"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "download");

		addItem(p);
	}

	private void addItemDownloadAllAsZIP(int p) {
		int i = listPopups.indexOf(POPUP_DOWNLOAD_ALL_AS_ZIP);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.downloadAllAsZip"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "download");

		addItem(p);
	}

	private void addItemCopyText(int p) {
		int i = listPopups.indexOf(POPUP_COPY);

		menuItems[i] = new JMenuItem(Configed.getResourceValue("copy"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "copy");
		menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

		addItem(p);
	}

	private void addItemPDF(int p) {
		int i = listPopups.indexOf(POPUP_PDF);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"));
		Icons.addThemeIconInvertedToMenuItem(menuItems[i], "anyType");

		addItem(p);
	}

	private void addItemExportCSV(int p) {
		int i = listPopups.indexOf(POPUP_EXPORT_CSV);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportTableAsCSV"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "export");

		addItem(p);
	}

	private void addItemExportSelectedCSV(int p) {
		int i = listPopups.indexOf(POPUP_EXPORT_SELECTED_CSV);
		menuItems[i] = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV"));
		Icons.addIntellijIconToMenuItem(menuItems[i], "export");

		addItem(p);
	}

	private void addItemDelete(int p) {
		int i = listPopups.indexOf(POPUP_DELETE);
		menuItems[i] = new JMenuItem();
		Icons.addIntellijIconToMenuItem(menuItems[i], "remove");
		menuItems[i].setEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		addItem(p);
	}

	private void addItemAdd(int p) {
		int i = listPopups.indexOf(POPUP_ADD);
		menuItems[i] = new JMenuItem();
		Icons.addIntellijIconToMenuItem(menuItems[i], "add");
		menuItems[i].setEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		addItem(p);
	}

	public void setText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			Logging.info(this, "setText - popup ", popup, " not in list");
			return;
		}

		menuItems[i].setText(s);
	}

	public void setToolTipText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			Logging.info(this, "setToolTipText - popup ", popup, " not in list");
			return;
		}

		menuItems[i].setToolTipText(s);
	}

	private void addItem(final int p) {
		int i = listPopups.indexOf(p);
		menuItems[i].addActionListener(actionEvent -> action(p));

		add(menuItems[i]);
	}

	// should be overwritten for specific actions in subclasses
	public void action(int p) {
		Logging.debug(this, "action called for type ", p);
	}
}
