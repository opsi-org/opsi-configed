/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.logging.Logging;

public final class PopupMenuTrait extends JPopupMenu {
	public enum PopupType {
		USER, USERS, ROLE, ROLES
	}

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
	private Map<Integer, Supplier<JMenuItem>> popupCreators = Map.ofEntries(
			Map.entry(POPUP_RELOAD, this::createItemReload), Map.entry(POPUP_FLOATING_COPY, this::addPopupFloatingCopy),
			Map.entry(POPUP_SAVE, PopupMenuTrait::addItemSave), Map.entry(POPUP_COPY, PopupMenuTrait::addItemCopyText),
			Map.entry(POPUP_DOWNLOAD, PopupMenuTrait::addItemDownload),
			Map.entry(POPUP_DOWNLOAD_AS_ZIP, PopupMenuTrait::addItemDownloadAsZIP),
			Map.entry(POPUP_DOWNLOAD_ALL_AS_ZIP, PopupMenuTrait::addItemDownloadAllAsZIP),
			Map.entry(POPUP_PDF, PopupMenuTrait::addItemPDF),
			Map.entry(POPUP_EXPORT_CSV, PopupMenuTrait::addItemExportCSV),
			Map.entry(POPUP_EXPORT_SELECTED_CSV, PopupMenuTrait::addItemExportSelectedCSV),
			Map.entry(POPUP_DELETE, this::addItemDelete), Map.entry(POPUP_ADD, this::addItemAdd));

	private JComponent component;
	private Map<Integer, Runnable> actions;
	private PopupType popupType;

	private PopupMenuTrait(JComponent component, Map<Integer, Runnable> actions, PopupType popupType) {
		this.component = component;
		this.actions = actions;
		this.popupType = popupType;

		// We want to have the popups in the order defined by the int constants
		for (int p : new TreeSet<>(actions.keySet())) {
			JMenuItem item = createPopupForAction(p);
			item.addActionListener(event -> actions.get(p).run());
			super.add(item);
		}
	}

	public static JPopupMenu createAndBindJPopupMenu(JComponent component, Map<Integer, Runnable> actions) {
		return createAndBindJPopupMenu(component, actions, null);
	}

	public static JPopupMenu createAndBindJPopupMenu(JComponent component, Map<Integer, Runnable> actions,
			Predicate<MouseEvent> condition) {
		return createAndBindJPopupMenu(component, actions, condition, null);
	}

	public static JPopupMenu createAndBindJPopupMenu(JComponent component, Map<Integer, Runnable> actions,
			Predicate<MouseEvent> condition, PopupType popupType) {
		JPopupMenu popupMenu = new PopupMenuTrait(component, actions, popupType);

		component.addMouseListener(new PopupMouseListener(popupMenu, condition));

		return popupMenu;
	}

	private JMenuItem createPopupForAction(final int p) {
		if (popupCreators.containsKey(p)) {
			return popupCreators.get(p).get();
		} else {
			Logging.warning(this, "popup ", p, " not defined in popupCreators");
			return new JMenuItem();
		}
	}

	private JMenuItem createItemReload() {
		String ressourceKey = popupType == null ? "reload" : "EditMapPanelGroupedForHostConfigs.reconstructUsers";

		JMenuItem item = new JMenuItem(Configed.getResourceValue(ressourceKey));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(item, "refresh");

		SwingUtils.addKeyBindingToJComponent(component, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				() -> actions.get(POPUP_RELOAD).run());

		return item;
	}

	private JMenuItem addPopupFloatingCopy() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.floatingInstance"));
		Icons.addIntellijIconToMenuItem(item, "copy");

		addSeparator();
		return item;
	}

	private static JMenuItem addItemSave() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("save"));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(item, "save");
		item.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
				.isGlobalReadOnly());

		return item;
	}

	private static JMenuItem addItemDownload() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("download"));
		Icons.addIntellijIconToMenuItem(item, "download");

		return item;
	}

	private static JMenuItem addItemDownloadAsZIP() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.downloadAsZip"));
		Icons.addIntellijIconToMenuItem(item, "download");

		return item;
	}

	private static JMenuItem addItemDownloadAllAsZIP() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.downloadAllAsZip"));
		Icons.addIntellijIconToMenuItem(item, "download");

		return item;
	}

	private static JMenuItem addItemCopyText() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("copy"));
		Icons.addIntellijIconToMenuItem(item, "copy");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

		return item;
	}

	private static JMenuItem addItemPDF() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"));
		Icons.addThemeIconInvertedToMenuItem(item, "anyType");

		return item;
	}

	private static JMenuItem addItemExportCSV() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportTableAsCSV"));
		Icons.addIntellijIconToMenuItem(item, "export");

		return item;
	}

	private static JMenuItem addItemExportSelectedCSV() {
		JMenuItem item = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV"));
		Icons.addIntellijIconToMenuItem(item, "export");

		return item;
	}

	private JMenuItem addItemDelete() {
		JMenuItem item = new JMenuItem();

		switch (popupType) {
		case USER:
			item.setText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser"));
			item.setToolTipText(
					Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser.ToolTip"));
			break;

		case ROLE:
			item.setText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole"));
			item.setToolTipText(
					Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole.ToolTip"));
			break;

		default:
			Logging.warning(this, "popupType ", popupType, " not defined for delete item");
			break;
		}

		Icons.addIntellijIconToMenuItem(item, "remove");
		item.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
				.isGlobalReadOnly());

		return item;

	}

	private JMenuItem addItemAdd() {
		JMenuItem item = new JMenuItem();

		switch (popupType) {
		case USER, USERS:
			item.setText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser"));
			item.setToolTipText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser.ToolTip"));
			break;

		case ROLE, ROLES:
			item.setText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole"));
			item.setToolTipText(Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole.ToolTip"));
			break;

		case null:
			Logging.warning(this, "popupType is null, cannot set text for add item");
			break;
		}

		Icons.addIntellijIconToMenuItem(item, "add");
		item.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
				.isGlobalReadOnly());
		return item;
	}
}
