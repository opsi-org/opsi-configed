/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class PanelGenEditPopupManager {
	public static final int POPUP_DELETE_ROW = 1;
	public static final int POPUP_CANCEL = 3;

	public static final int POPUP_SORT_AGAIN = 5;

	private static final int[] POPUPS_EXPORT = new int[] { PopupMenuTrait.POPUP_SEPARATOR,
			PopupMenuTrait.POPUP_EXPORT_CSV, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV };

	private static final Map<Integer, String> keyNames = new HashMap<Integer, String>() {
		@Override
		public String put(Integer key, String value) {
			// checking that not the same int key is used twice

			if (get(key) != null) {
				Logging.error("duplicate key setting ", key, ", until now ", get(key), " now ", value);
			}
			return super.put(key, value);
		}
	};

	private int popupIndex;

	private List<Integer> internalpopups;

	private JMenuItem menuItemDeleteRelation;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemCancel;

	private JPopupMenu popupMenu;

	private int generalPopupPosition;

	private PanelGenEdit panelGenEdit;

	private Map<Integer, Runnable> popupCreators = Map.of(PopupMenuTrait.POPUP_SEPARATOR, () -> addPopupItem(null),
			PopupMenuTrait.POPUP_SAVE, () -> {
				menuItemSave = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.saveData"));
				menuItemSave.setEnabled(false);
				menuItemSave.addActionListener(actionEvent -> panelGenEdit.commit());
				addPopupItem(menuItemSave);
			}, POPUP_CANCEL, () -> {
				menuItemCancel = new JMenuItem(Configed.getResourceValue("discard"));
				menuItemCancel.setEnabled(false);
				menuItemCancel.addActionListener(actionEvent -> panelGenEdit.cancel());
				addPopupItem(menuItemCancel);
			}, PopupMenuTrait.POPUP_RELOAD, () -> addPopupItemReload(), POPUP_SORT_AGAIN, () -> {
				JMenuItem menuItemSortAgain = new JMenuItem(
						Configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
				menuItemSortAgain
						.addActionListener(actionEvent -> panelGenEdit.getGenEditTable().sortAgainAsConfigured());

				addPopupItem(menuItemSortAgain);
			}, POPUP_DELETE_ROW, () -> addPopupMenuDeleteRow(), PopupMenuTrait.POPUP_PRINT, () -> {
				JMenuItem menuItemPrint = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.print"));
				Icons.addIntellijIconToMenuItem(menuItemPrint, "print");
				menuItemPrint.addActionListener(actionEvent -> print());

				addPopupItem(menuItemPrint);
			}, PopupMenuTrait.POPUP_EXPORT_CSV, () -> {
				ExporterToCSV exportTable = new ExporterToCSV(panelGenEdit.getGenEditTable());
				JMenuItem menuItemExportCSV = exportTable.getMenuItemExport();
				addPopupItem(menuItemExportCSV);
			}, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV, () -> {
				ExporterToCSV exportTable = new ExporterToCSV(panelGenEdit.getGenEditTable());
				JMenuItem menuItemExportSelectedCSV = exportTable.getMenuItemExportSelected();
				addPopupItem(menuItemExportSelectedCSV);
			}, PopupMenuTrait.POPUP_PDF, () -> {
				JMenuItem menuItemPDF = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"));
				Icons.addThemeIconInvertedToMenuItem(menuItemPDF, "anyType");
				menuItemPDF.addActionListener(actionEvent -> exportTable());

				addPopupItem(menuItemPDF);
			});

	public PanelGenEditPopupManager(PanelGenEdit panelGenEdit, int generalPopupPosition, int[] popupsWanted) {
		this.panelGenEdit = panelGenEdit;
		this.generalPopupPosition = generalPopupPosition;
		this.internalpopups = new ArrayList<>();

		if (popupsWanted != null) {
			for (int wantedPopup : popupsWanted) {
				this.internalpopups.add(wantedPopup);
				Logging.info(this, "add popup ", wantedPopup);
			}
		} else {
			this.internalpopups.add(PopupMenuTrait.POPUP_RELOAD);

			this.internalpopups.add(PopupMenuTrait.POPUP_PDF);
		}

		Logging.info(this, "internalpopups ", giveMenuitemNames(internalpopups));

		this.internalpopups = supplementBefore(PopupMenuTrait.POPUP_RELOAD, POPUPS_EXPORT, this.internalpopups);

		Logging.info(this, "internalpopups supplemented ", giveMenuitemNames(internalpopups));
	}

	public void setDataChanged(boolean dataChanged) {
		if (menuItemSave != null) {
			menuItemSave.setEnabled(dataChanged);
		}
		if (menuItemCancel != null) {
			menuItemCancel.setEnabled(dataChanged);
		}
	}

	private static List<Integer> supplementBefore(int insertpoint, final int[] injectKeys,
			final List<Integer> listOfKeys) {
		List<Integer> augmentedList = new ArrayList<>();

		boolean found = false;

		Set<Integer> setOfKeys = new HashSet<>();

		for (int key : listOfKeys) {
			if (key == insertpoint) {
				found = true;
				addMissingKeys(injectKeys, setOfKeys, augmentedList);
			}

			augmentedList.add(key);
			setOfKeys.add(key);
		}

		if (!found) {
			addMissingKeys(injectKeys, setOfKeys, augmentedList);
		}

		return augmentedList;
	}

	private void addPopupMenuDeleteRow() {
		menuItemDeleteRelation = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.deleteRow"));
		menuItemDeleteRelation.setEnabled(false);
		menuItemDeleteRelation.addActionListener(actionEvent -> panelGenEdit.getGenEditTable().deleteRelation());
		addPopupItem(menuItemDeleteRelation);
	}

	private void addPopupItemReload() {
		JMenuItem menuItemReload = new JMenuItem(Configed.getResourceValue("reload"));
		menuItemReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(menuItemReload, "refresh");

		Utils.addKeyBindingToJComponent(panelGenEdit, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), panelGenEdit::reload);
		// does not work
		menuItemReload.addActionListener(actionEvent -> panelGenEdit.reload());
		if (popupIndex > 1) {
			popupMenu.addSeparator();
		}

		addPopupItem(menuItemReload);
	}

	public void enableMenuItemDeleteRelation(boolean enabled) {
		if (menuItemDeleteRelation != null) {
			menuItemDeleteRelation.setEnabled(enabled);
		}
	}

	public void addPopupItem(JMenuItem item) {
		if (popupMenu == null) {
			// for the first item, we create the menu
			popupMenu = new JPopupMenu();
			PopupMouseListener.addPopupMouseListenerToComponents(popupMenu,
					new JComponent[] { panelGenEdit.getGenEditTable(), panelGenEdit.getTheScrollpane() });
		}

		if (item == null) {
			if (popupIndex > 1) {
				popupMenu.addSeparator();
			}

			return;
		}

		popupMenu.add(item);

		// prevents circle
		popupIndex++;
		if (popupIndex == generalPopupPosition) {
			addPopupmenuStandardpart();
		}
	}

	private static void addMissingKeys(int[] injectKeys, Set<Integer> setOfKeys, List<Integer> augmentedList) {
		for (int type : injectKeys) {
			if (!setOfKeys.contains(type)) {
				augmentedList.add(type);
				setOfKeys.add(type);
			}
		}
	}

	private void print() {
		try {
			panelGenEdit.getGenEditTable().print();
		} catch (PrinterException ex) {
			Logging.error(ex, "Printing error ");
		}
	}

	private void exportTable() {
		Map<String, String> metaData = new HashMap<>();
		metaData.put("header", panelGenEdit.getTitle());
		metaData.put("subject", "report of table");
		metaData.put("keywords", "");

		ExporterToPDF pdfExportTable = new ExporterToPDF(panelGenEdit.getGenEditTable());
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	public void addPopupmenuStandardpart() {
		if (generalPopupPosition != 0) {
			// if -1 dont use a standard popup
			// if > 0 the popup is added later after installing another popup
			return;
		}
		Logging.info(this, "addPopupmenuStandardpart, internalpopups ", giveMenuitemNames(internalpopups));

		if (generalPopupPosition > 0) {
			// add separator if a real position is given
			popupMenu.addSeparator();
		}

		internalpopups = supplementBefore(PopupMenuTrait.POPUP_RELOAD, POPUPS_EXPORT, internalpopups);

		Logging.info(this, "addPopupmenuStandardpart, supplemented internalpopups ", giveMenuitemNames(internalpopups));

		for (int popuptype : internalpopups) {
			Runnable popupCreator = popupCreators.get(popuptype);
			if (popupCreator == null) {
				Logging.warning(this, "no popup factory found for popuptype ", popuptype);
				continue;
			}

			popupCreator.run();
		}
	}

	private static final List<String> giveMenuitemNames(List<Integer> popups) {
		List<String> result = new ArrayList<>();

		for (int el : popups) {
			result.add(keyNames.get(el));
		}

		return result;
	}
}
