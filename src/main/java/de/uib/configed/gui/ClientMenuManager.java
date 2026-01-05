/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uib.configed.core.domain.permission.UserServerConsoleConfig;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.configed.gui.features.terminal.TerminalController;
import de.uib.configed.gui.share.swing.JMenuItemBlockedKeyBinding;
import de.uib.configed.gui.share.table.AbstractExportTable;
import de.uib.configed.gui.share.table.ClientTableExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.HostInfo.ColumnDisplayInfo;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class ClientMenuManager implements MenuListener {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Set<JMenuItem> clientMenuItemsDependOnSelectionCount = new HashSet<>();

	private final Map<String, JMenuItem> clientMenuItems = new LinkedHashMap<>();

	private JMenu jMenuClients = new JMenu(Configed.getResourceValue("MainFrame.jMenuClients"));

	private Map<String, JMenuItem> menuItemsHost;
	private ConfigedMain configedMain;

	private MainFrame mainFrame;

	private ClientMenuManager(ConfigedMain configedMain, MainFrame mainFrame) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;

		menuItemsHost = new LinkedHashMap<>();
		menuItemsHost.put(UserRolesConfigDataService.ITEM_ADD_CLIENT, clientMenuItems.get("MainFrame.jMenuAddClient"));
		menuItemsHost.put(UserRolesConfigDataService.ITEM_DELETE_CLIENT,
				clientMenuItems.get("MainFrame.jMenuDeleteClient"));
		menuItemsHost.put(UserRolesConfigDataService.ITEM_FREE_LICENSES,
				clientMenuItems.get("MainFrame.jMenuFreeLicenses"));

		initJMenu();

		mainFrame.getClientTablePanel().getClientTable().getTableHeader()
				.setComponentPopupMenu(getPopupMenuClone((JMenu) clientMenuItems.get("MainFrame.jMenuShowColumns")));
	}

	public static ClientMenuManager getNewInstance(ConfigedMain configedMain, MainFrame mainFrame) {
		return new ClientMenuManager(configedMain, mainFrame);
	}

	public JMenu getJMenu() {
		return jMenuClients;
	}

	private void addClientActions() {
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuWakeOnLan", ServerActionManager::wakeSelectedClients).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createSubMenu(ClientMenuItemConfig
				.submenu("MainFrame.jMenuOpsiClientdEvent", this::initOpsiclientdEventMenu).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuShowPopupMessage", this::showPopupOnClientsAction).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuRequestSessionInfo", () -> SessionInfoRetriever.retrieveSessionInfo(configedMain))
				.withIcon("user").withInvertedIcon(true).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuDeletePackageCaches", ServerActionManager::deletePackageCachesOfSelectedClients)
				.dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
	}

	private void addClientSystemManagementActions() {
		jMenuClients.add(createMenuItem(
				ClientMenuItemConfig.item("MainFrame.jMenuShutdownClient", ServerActionManager::shutdownSelectedClients)
						.dependOnSelectionCount(true)
						.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(
				ClientMenuItemConfig.item("MainFrame.jMenuRebootClient", ServerActionManager::rebootSelectedClients)
						.dependOnSelectionCount(true)
						.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(
				ClientMenuItemConfig.item("MainFrame.jMenuOpenTerminal", TerminalController::openTerminalOnClient)
						.withIcon("terminal").dependOnSelectionCount(true)
						.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));

		JMenuItem jMenuRemoteControl = createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuRemoteControl",
						() -> ExtraFrameController.startRemoteControlFrame(configedMain, persistenceController))
				.dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly()));

		// Space should only be active on the client table, but not on other where you 
		// could accidently start remote control by pressing space in a text field etc.
		Utils.addKeyBindingToJComponent(mainFrame.getClientTablePanel().getClientTable(),
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				() -> ExtraFrameController.startRemoteControlFrame(configedMain, persistenceController));

		// We want to add the acceserator manually so that it will be active always, not only
		// when the client table has focus.
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		jMenuClients.add(jMenuRemoteControl);
	}

	private void addClientAdministrationActions() {
		jMenuClients.add(createMenuItem(
				ClientMenuItemConfig.item("MainFrame.jMenuAddClient", ExtraFrameController::callAddClientDialog)
						.withIcon("add").dependOnSelectionCount(true)
						.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuCopyClient", ServerActionManager::copySelectedClient).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(
				ClientMenuItemConfig.item("MainFrame.jMenuDeleteClient", ServerActionManager::deleteSelectedClients)
						.withIcon("delete").dependOnSelectionCount(true)
						.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createSubMenu(ClientMenuItemConfig
				.submenu("MainFrame.jMenuResetProducts", () -> initResetProductsMenu()).dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuFreeLicenses", ServerActionManager::freeAllPossibleLicensesForSelectedClients)
				.dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuChangeClientID", ServerActionManager::callChangeClientIDDialog)
				.dependOnSelectionCount(true)
				.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));

		// is multiDepot
		if (persistenceController.getHostInfoCollections().getDepots().size() != 1) {
			jMenuClients.add(createMenuItem(
					ClientMenuItemConfig.item("MainFrame.jMenuChangeDepot", ServerActionManager::callChangeDepotDialog)
							.dependOnSelectionCount(true)
							.readOnly(persistenceController.getUserRolesConfigDataService().isGlobalReadOnly())));
		}
	}

	private void addClientSearchActions() {
		jMenuClients.add(createMenuItem(ClientMenuItemConfig.item("MainFrame.jMenuClientselectionGetGroup",
				() -> ExtraFrameController.callClientSelectionDialog(configedMain))));
		jMenuClients.add(createMenuItem(ClientMenuItemConfig.item("MainFrame.jMenuClientselectionGetSavedSearch",
				() -> ExtraFrameController.clientSelectionGetSavedSearch(configedMain))));
	}

	private void addClientTableActions() {
		jMenuClients.add(createMenuItem(ClientMenuItemConfig.item("reload", configedMain::reloadHosts)
				.withIcon("refresh").withKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))));

		jMenuClients.add(
				createSubMenu(ClientMenuItemConfig.submenu("MainFrame.jMenuShowColumns", this::initShowColumnsMenu)));

		jMenuClients.add(createMenuItem(ClientMenuItemConfig
				.item("MainFrame.jMenuInvertSelection", configedMain::invertSelection).withKeyStroke(KeyStroke
						.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK))));
	}

	private void addClientExportActions() {
		jMenuClients.add(
				createMenuItem(ClientMenuItemConfig.item("FGeneralDialog.pdf", this::createPdf).withIcon("anyType")));

		AbstractExportTable exportTable = new ExporterToCSV(mainFrame.getClientTablePanel().getClientTable());
		exportTable.addMenuItemsTo(jMenuClients);

		ClientTableExporterToCSV clientTableExporter = new ClientTableExporterToCSV(
				mainFrame.getClientTablePanel().getClientTable());
		clientTableExporter.addMenuItemsTo(jMenuClients);
	}

	private void initJMenu() {
		jMenuClients.addMenuListener(this);

		addClientActions();

		jMenuClients.addSeparator();

		addClientSystemManagementActions();

		jMenuClients.addSeparator();

		addClientAdministrationActions();

		jMenuClients.addSeparator();

		addClientSearchActions();

		jMenuClients.addSeparator();

		addClientTableActions();

		jMenuClients.addSeparator();

		addClientExportActions();
	}

	private JMenuItem createMenuItem(ClientMenuItemConfig config) {
		JMenuItem item;

		if (config.keyStroke() != null) {
			item = new JMenuItemBlockedKeyBinding(Configed.getResourceValue(config.resourceKey()));
			item.setAccelerator(config.keyStroke());
			Utils.addKeyBindingToJComponent(mainFrame.getClientConfiguration().getPanelClientSelection(),
					config.keyStroke(), config.action());
		} else {
			item = new JMenuItem(Configed.getResourceValue(config.resourceKey()));
		}

		if (config.icon() != null) {
			if (config.invertedIcon()) {
				Icons.addThemeIconInvertedToMenuItem(item, config.icon());
			} else {
				Icons.addIntellijIconToMenuItem(item, config.icon());
			}
		}

		if (config.action() != null) {
			item.addActionListener(e -> config.action().run());
		}

		if (config.dependOnSelectionCount()) {
			clientMenuItemsDependOnSelectionCount.add(item);
		}

		item.setEnabled(!config.readOnly());
		clientMenuItems.put(config.resourceKey(), item);
		return item;
	}

	private JMenu createSubMenu(ClientMenuItemConfig config) {
		JMenu menu = config.subMenuSupplier().get();
		if (config.dependOnSelectionCount()) {
			clientMenuItemsDependOnSelectionCount.add(menu);
		}
		menu.setEnabled(!config.readOnly());
		clientMenuItems.put(config.resourceKey(), menu);
		return menu;
	}

	private JMenu initOpsiclientdEventMenu() {
		JMenu menu = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));
		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.addActionListener(actionEvent -> ServerActionManager.fireOpsiclientdEventOnSelectedClients(event));
			menu.add(item);
		}
		return menu;
	}

	private static JMenu initResetProductsMenu() {
		return createResetProductsMenuItemsTo();
	}

	public static JMenu createResetProductsMenuItemsTo() {
		return createResetProductsMenuItemsTo(true, true, true);
	}

	public static JMenu createResetLocalbootProductsMenuItemsTo() {
		return createResetProductsMenuItemsTo(true, false, false);
	}

	public static JMenu createResetNetbootProductsMenuItemsTo() {
		return createResetProductsMenuItemsTo(false, true, false);
	}

	private static JMenu createResetProductsMenuItemsTo(boolean includeResetOptionForLocalbootProducts,
			boolean includeResetOptionForNetbootProducts, boolean includeResetOptionForBothProducts) {
		JMenu jMenu = new JMenu(Configed.getResourceValue("MainFrame.jMenuResetProducts"));

		if (includeResetOptionForLocalbootProducts) {
			JMenuItem jMenuResetLocalbootProductOnClientWithStates = new JMenuItem(
					Configed.getResourceValue("localbootProducts"));
			jMenuResetLocalbootProductOnClientWithStates
					.addActionListener(event -> resetProductOnClientAction(true, true, false));

			JMenuItem jMenuResetLocalbootProductOnClient = new JMenuItem(
					Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithoutStates"));
			jMenuResetLocalbootProductOnClient
					.addActionListener(event -> resetProductOnClientAction(false, true, false));

			jMenu.add(jMenuResetLocalbootProductOnClientWithStates);
			jMenu.add(jMenuResetLocalbootProductOnClient);
		}
		if (includeResetOptionForNetbootProducts) {
			JMenuItem jMenuResetNetbootProductOnClientWithStates = new JMenuItem(
					Configed.getResourceValue("netbootProducts"));
			jMenuResetNetbootProductOnClientWithStates
					.addActionListener(event -> resetProductOnClientAction(true, false, true));

			JMenuItem jMenuResetNetbootProductOnClient = new JMenuItem(
					Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithoutStates"));
			jMenuResetNetbootProductOnClient.addActionListener(event -> resetProductOnClientAction(false, false, true));

			jMenu.add(jMenuResetNetbootProductOnClientWithStates);
			jMenu.add(jMenuResetNetbootProductOnClient);
		}
		if (includeResetOptionForBothProducts) {
			JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem(
					Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));
			jMenuResetProductOnClientWithStates
					.addActionListener(event -> resetProductOnClientAction(true, true, true));

			JMenuItem jMenuResetProductOnClient = new JMenuItem(
					Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));
			jMenuResetProductOnClient.addActionListener(event -> resetProductOnClientAction(false, true, true));

			jMenu.add(jMenuResetProductOnClientWithStates);
			jMenu.add(jMenuResetProductOnClient);
		}

		return jMenu;
	}

	private JMenu initShowColumnsMenu() {
		JMenu jMenu = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		for (ColumnDisplayInfo info : HostInfo.ORDERED_DISPLAY_COLUMN_INFOS) {
			JCheckBoxMenuItem jCheckBoxMenuItem = createShowColumnCheckBoxMenuItem(info);
			if (jCheckBoxMenuItem != null) {
				jMenu.add(jCheckBoxMenuItem);
			}
		}

		return jMenu;
	}

	private JCheckBoxMenuItem createShowColumnCheckBoxMenuItem(ColumnDisplayInfo info) {
		String resourceKey = info.resourceKey;
		if (resourceKey == null) {
			Logging.info(this, "Label ", info.label,
					" has no resourceKey because we want it not to be shown - skipping");
			return null;
		}
		String menuLabel = Configed.getResourceValue(resourceKey);
		boolean selected = false;
		if (persistenceController.getHostDataService().getHostDisplayFields().containsKey(info.label)) {
			Boolean val = persistenceController.getHostDataService().getHostDisplayFields().get(info.label);
			selected = Boolean.TRUE.equals(val);
		}

		JCheckBoxMenuItem item = new JCheckBoxMenuItem(menuLabel);
		item.setSelected(selected);

		if (info.label.equals(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)) {
			item.addActionListener((ActionEvent event) -> {
				ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
						.hideHealthCheckActiveCheckBox(Boolean.FALSE.equals(persistenceController.getHostDataService()
								.getHostDisplayFields().get(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)));
				configedMain.toggleColumn(info.label);
			});
			return item;
		}

		item.addActionListener(event -> configedMain.toggleColumn(info.label));
		return item;
	}

	private void createPdf() {
		Map<String, String> metaData = new HashMap<>();
		String title = Configed.getResourceValue("MainFrame.ClientList");

		metaData.put("header", title);
		metaData.put("title", title);
		metaData.put("subject", "report of table");

		ExporterToPDF pdfExportTable = new ExporterToPDF(mainFrame.getClientTablePanel().getClientTable());

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	private void showPopupOnClientsAction() {
		JTextField durationTextField = new JTextField();
		JTextArea messageTextArea = new JTextArea();
		messageTextArea.setColumns(30);
		messageTextArea.setRows(8);
		messageTextArea.setLineWrap(true);

		JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
		messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		Object[] message = { Configed.getResourceValue("MainFrame.writePopupDuration"), durationTextField,
				Configed.getResourceValue("MainFrame.writePopupMessage"), messageScrollPane };

		int result = JOptionPane.showConfirmDialog(mainFrame, message,
				Configed.getResourceValue("MainFrame.popupFrameTitle"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (result == 0) {
			Float duration = 0F;
			if (!durationTextField.getText().isEmpty()) {
				duration = Float.parseFloat(durationTextField.getText());
			}
			ServerActionManager.showPopupOnSelectedClients(messageTextArea.getText(), duration);
		}
	}

	private static void resetProductOnClientAction(boolean withProductProperties, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		ServerActionManager.resetProductsForSelectedClients(withProductProperties, resetLocalbootProducts,
				resetNetbootProducts);
	}

	private void enableMenuItemsForClients() {
		int countSelectedClients = configedMain.getSelectedClients().size();
		Logging.debug(" enableMenuItemsForClients, countSelectedClients ", countSelectedClients);

		for (JMenuItem jMenuItem : clientMenuItemsDependOnSelectionCount) {
			if (clientMenuItemsDependOnSelectionCount.contains(jMenuItem)
					&& persistenceController.getUserRolesConfigDataService().isGlobalReadOnly()) {
				jMenuItem.setEnabled(false);
			} else {
				jMenuItem.setEnabled(countSelectedClients >= 1);
			}
		}

		clientMenuItems.get("MainFrame.jMenuChangeClientID").setEnabled(
				countSelectedClients == 1 && !persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		clientMenuItems.get("MainFrame.jMenuCopyClient").setEnabled(
				countSelectedClients == 1 && !persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());

		List<Object> forbiddenItems = persistenceController.getUserRolesConfigDataService().terminalsForbidden();

		if (forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CLIENTS)
				|| !persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)
				|| persistenceController.getUserRolesConfigDataService().isGlobalReadOnly()) {
			clientMenuItems.get("MainFrame.jMenuOpenTerminal").setEnabled(false);
			clientMenuItems.get("MainFrame.jMenuOpenTerminal")
					.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminal")
							+ Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden"));
		} else {
			clientMenuItems.get("MainFrame.jMenuOpenTerminal")
					.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminal"));
			clientMenuItems.get("MainFrame.jMenuOpenTerminal").setEnabled(countSelectedClients == 1);
		}

		checkMenuItemsDisabling();
	}

	private void checkMenuItemsDisabling() {
		List<String> disabledClientMenuEntries = persistenceController.getConfigDataService()
				.getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {
			for (String menuActionType : disabledClientMenuEntries) {
				JMenuItem menuItem = menuItemsHost.get(menuActionType);
				Logging.debug("disable ", menuActionType, ", ", menuItem);
				if (menuItem != null) {
					menuItem.setEnabled(false);
				} else {
					Logging.warning(this, "Cannot disable menuItem", menuActionType, ", it does not exist");
				}
			}

			if (!persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD()
					&& persistenceController.getUserRolesConfigDataService().isGlobalReadOnly()) {
				clientMenuItems.get("MainFrame.jMenuCopyClient").setEnabled(false);
			}

			if (persistenceController.getConfigDataService().getDisabledClientMenuEntries()
					.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT)) {
				clientMenuItems.get("MainFrame.jMenuAddClient").setEnabled(false);
			} else {
				clientMenuItems.get("MainFrame.jMenuAddClient")
						.setEnabled(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD()
								&& !persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
			}
		}
	}

	public static JPopupMenu getPopupMenuClone(JMenu jMenuToClone) {
		return clonePopupMenu(jMenuToClone, null);
	}

	public JPopupMenu getPopupMenuClone() {
		return clonePopupMenu(jMenuClients, this::enableMenuItemsForClients);
	}

	private static JPopupMenu clonePopupMenu(JMenu jMenuToClone, Runnable beforeCloneAction) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				if (beforeCloneAction != null) {
					beforeCloneAction.run();
				}
				popupMenu.removeAll();
				cloneMenuItems(popupMenu, jMenuToClone);
			}

		});
		return popupMenu;
	}

	private static void cloneMenuItems(JPopupMenu popupMenu, JMenu menuToCopy) {
		for (int i = 0; i < menuToCopy.getItemCount(); i++) {
			Component component = menuToCopy.getMenuComponent(i);
			if (component instanceof JSeparator) {
				popupMenu.addSeparator();
			}

			if (component instanceof JMenuItem jMenuItem) {
				popupMenu.add(cloneMenuItem(jMenuItem));
			}
		}
	}

	private static JMenuItem cloneMenuItem(JMenuItem sourceItem) {
		JMenuItem clonedItem;
		if (sourceItem instanceof JMenu sourceSubMenu) {
			clonedItem = new JMenu(sourceSubMenu.getText());
			JMenu targetSubMenu = (JMenu) clonedItem;
			targetSubMenu.setEnabled(sourceSubMenu.isEnabled());
			for (int i = 0; i < sourceSubMenu.getItemCount(); i++) {
				JMenuItem sourceSubItem = sourceSubMenu.getItem(i);
				if (sourceSubItem != null) {
					JMenuItem clonedSubItem = cloneMenuItem(sourceSubItem);
					clonedSubItem.setEnabled(sourceSubItem.isEnabled());
					targetSubMenu.add(clonedSubItem);
				}
			}
		} else if (sourceItem instanceof JCheckBoxMenuItem) {
			clonedItem = new JCheckBoxMenuItem(sourceItem.getText());
			clonedItem.setEnabled(sourceItem.isEnabled());
			clonedItem.setSelected(sourceItem.isSelected());
			clonedItem.addItemListener(event -> sourceItem.setSelected(clonedItem.isSelected()));
			sourceItem.addItemListener(event -> clonedItem.setSelected(sourceItem.isSelected()));
		} else {
			clonedItem = new JMenuItem(sourceItem.getText(), sourceItem.getIcon());
			clonedItem.setSelectedIcon(sourceItem.getSelectedIcon());
			clonedItem.setAccelerator(sourceItem.getAccelerator());
			clonedItem.setEnabled(sourceItem.isEnabled());
		}
		for (ActionListener listener : sourceItem.getActionListeners()) {
			clonedItem.addActionListener(listener);
		}

		return clonedItem;
	}

	@Override
	public void menuCanceled(MenuEvent arg0) {
		// Nothing to do.
	}

	@Override
	public void menuDeselected(MenuEvent arg0) {
		// Nothing to do.
	}

	@Override
	public void menuSelected(MenuEvent arg0) {
		enableMenuItemsForClients();
	}
}
