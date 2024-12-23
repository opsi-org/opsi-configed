/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.ServerActionManager;
import de.uib.configed.SessionInfoRetriever;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserServerConsoleConfig;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.AbstractExportTable;
import de.uib.utils.table.ClientTableExporterToCSV;
import de.uib.utils.table.ExporterToCSV;
import de.uib.utils.table.ExporterToPDF;

@SuppressWarnings({ "java:S1200" })
public final class ClientMenuManager implements MenuListener {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private JMenuItem jMenuRemoteControl = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
	private JMenuItem jMenuShowPopupMessage = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
	private JMenuItem jMenuRequestSessionInfo = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
	private JMenuItem jMenuShutdownClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuShutdownClient"));
	private JMenuItem jMenuRebootClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuRebootClient"));
	private JMenuItem jMenuOpenTerminalOnClient = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuOpenTerminal"));
	private JMenuItem jMenuChangeDepot = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuChangeDepot"));
	private JMenuItem jMenuChangeClientID = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuChangeClientID"));
	private JMenuItem jMenuAddClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuAddClient"));
	private JMenuItem jMenuDeleteClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuDeleteClient"));
	private JMenuItem jMenuCopyClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuCopyClient"));
	private JMenu jMenuResetProducts = initResetProductsMenu();
	private JMenuItem jMenuFreeLicenses = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFreeLicenses"));
	private JMenuItem jMenuDeletePackageCaches = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));

	private JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProducts, jMenuDeleteClient,
			jMenuFreeLicenses, jMenuShowPopupMessage, jMenuRequestSessionInfo, jMenuDeletePackageCaches,
			jMenuRebootClient, jMenuShutdownClient, jMenuChangeDepot, jMenuRemoteControl };

	private JMenu jMenuClients = new JMenu(Configed.getResourceValue("MainFrame.jMenuClients"));

	private Map<String, JMenuItem> menuItemsHost;
	private ConfigedMain configedMain;
	private MainFrame mainFrame;

	private ClientMenuManager(ConfigedMain configedMain, MainFrame mainFrame) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;

		menuItemsHost = new LinkedHashMap<>();
		menuItemsHost.put(UserRolesConfigDataService.ITEM_ADD_CLIENT, jMenuAddClient);
		menuItemsHost.put(UserRolesConfigDataService.ITEM_DELETE_CLIENT, jMenuDeleteClient);
		menuItemsHost.put(UserRolesConfigDataService.ITEM_FREE_LICENSES, jMenuFreeLicenses);

		initJMenu();
	}

	public static ClientMenuManager getNewInstance(ConfigedMain configedMain, MainFrame mainFrame) {
		return new ClientMenuManager(configedMain, mainFrame);
	}

	public JMenu getJMenu() {
		return jMenuClients;
	}

	private void initJMenu() {
		jMenuClients.addMenuListener(this);

		jMenuChangeDepot.addActionListener(event -> ServerActionManager.callChangeDepotDialog());
		jMenuChangeClientID.addActionListener(event -> ServerActionManager.callChangeClientIDDialog());

		JMenuItem jMenuSelectionGetGroup = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuSelectionGetGroup.addActionListener(event -> ExtraFrameController.callClientSelectionDialog(configedMain));

		JMenuItem jMenuSelectionGetSavedSearch = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuSelectionGetSavedSearch
				.addActionListener(event -> ExtraFrameController.clientSelectionGetSavedSearch(configedMain));

		JMenuItem jMenuRebuildClientList = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.reload"));
		Icons.addIntellijIconToMenuItem(jMenuRebuildClientList, "refresh");
		jMenuRebuildClientList.addActionListener(event -> configedMain.reloadHosts());

		JMenuItem jMenuCreatePdf = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"));
		Icons.addThemeIconInvertedToMenuItem(jMenuCreatePdf, "anyType");
		jMenuCreatePdf.addActionListener(event -> createPdf());

		Icons.addIntellijIconToMenuItem(jMenuAddClient, "add");
		jMenuAddClient.addActionListener(event -> ExtraFrameController.callNewClientDialog());

		jMenuDeletePackageCaches.addActionListener(event -> ServerActionManager.deletePackageCachesOfSelectedClients());

		JMenuItem jMenuWakeOnLan = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
		jMenuWakeOnLan.addActionListener(event -> ServerActionManager.wakeSelectedClients());

		JMenu jMenuOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.addActionListener(actionEvent -> ServerActionManager.fireOpsiclientdEventOnSelectedClients(event));
			jMenuOpsiClientdEvent.add(item);
		}

		jMenuShowPopupMessage.addActionListener(event -> showPopupOnClientsAction());
		jMenuShutdownClient.addActionListener(event -> ServerActionManager.shutdownSelectedClients());
		jMenuRequestSessionInfo.addActionListener(event -> SessionInfoRetriever.retrieveSessionInfo(configedMain));
		Icons.addThemeIconInvertedToMenuItem(jMenuRequestSessionInfo, "user");
		jMenuRebootClient.addActionListener(event -> ServerActionManager.rebootSelectedClients());

		Icons.addIntellijIconToMenuItem(jMenuDeleteClient, "delete");
		jMenuDeleteClient.addActionListener(event -> ServerActionManager.deleteSelectedClients());

		jMenuCopyClient.addActionListener(event -> ServerActionManager.copySelectedClient());
		jMenuFreeLicenses.addActionListener(event -> ServerActionManager.freeAllPossibleLicensesForSelectedClients());
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		jMenuRemoteControl.addActionListener(
				event -> ExtraFrameController.startRemoteControlFrame(configedMain, persistenceController));

		Icons.addIntellijIconToMenuItem(jMenuOpenTerminalOnClient, "terminal");
		jMenuOpenTerminalOnClient.addActionListener(event -> configedMain.openTerminalOnClient());

		jMenuClients.add(jMenuWakeOnLan);
		jMenuClients.add(jMenuOpsiClientdEvent);
		jMenuClients.add(jMenuShowPopupMessage);
		jMenuClients.add(jMenuRequestSessionInfo);
		jMenuClients.add(jMenuDeletePackageCaches);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuShutdownClient);
		jMenuClients.add(jMenuRebootClient);
		jMenuClients.add(jMenuOpenTerminalOnClient);
		jMenuClients.add(jMenuRemoteControl);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuAddClient);
		jMenuClients.add(jMenuCopyClient);
		jMenuClients.add(jMenuDeleteClient);

		jMenuClients.add(initResetProductsMenu());

		jMenuClients.add(jMenuFreeLicenses);
		jMenuClients.add(jMenuChangeClientID);

		// is multiDepot
		if (persistenceController.getHostInfoCollections().getDepots().size() != 1) {
			jMenuClients.add(jMenuChangeDepot);
		}
		jMenuClients.addSeparator();

		jMenuClients.add(jMenuSelectionGetGroup);
		jMenuClients.add(jMenuSelectionGetSavedSearch);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuRebuildClientList);
		jMenuClients.add(jMenuCreatePdf);

		AbstractExportTable exportTable = new ExporterToCSV(mainFrame.getClientTablePanel().getClientTable());
		exportTable.addMenuItemsTo(jMenuClients);

		ClientTableExporterToCSV clientTableExporter = new ClientTableExporterToCSV(
				mainFrame.getClientTablePanel().getClientTable());
		clientTableExporter.addMenuItemsTo(jMenuClients);

		jMenuClients.addSeparator();

		jMenuClients.add(initShowColumnsMenu());
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
					Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithStates"));
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
					Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithStates"));
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

	@SuppressWarnings({ "java:S138" })
	private JMenu initShowColumnsMenu() {
		JCheckBoxMenuItem jCheckBoxMenuItemShowCreatedColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		jCheckBoxMenuItemShowCreatedColumn.setSelected(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowCreatedColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowWANactiveColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		jCheckBoxMenuItemShowWANactiveColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowWANactiveColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowIPAddressColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		jCheckBoxMenuItemShowIPAddressColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowIPAddressColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowSystemUUIDColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowSystemUUIDColumn"));
		jCheckBoxMenuItemShowSystemUUIDColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSystemUUIDColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowHardwareAddressColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		jCheckBoxMenuItemShowHardwareAddressColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowHardwareAddressColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowSessionInfoColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		jCheckBoxMenuItemShowSessionInfoColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSessionInfoColumn.addActionListener(
				event -> configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowInventoryNumberColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		jCheckBoxMenuItemShowInventoryNumberColumn.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInventoryNumberColumn.addActionListener(
				event -> configedMain.toggleColumn(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowInstallByShutdown = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		jCheckBoxMenuItemShowInstallByShutdown.setSelected(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInstallByShutdown.addActionListener(
				event -> configedMain.toggleColumn(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowDepotColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		jCheckBoxMenuItemShowDepotColumn.setSelected(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowDepotColumn
				.addActionListener(event -> configedMain.toggleColumn(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

		JMenu jMenuShowColumns = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		jMenuShowColumns.add(jCheckBoxMenuItemShowWANactiveColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowIPAddressColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowSystemUUIDColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowHardwareAddressColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowSessionInfoColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowInventoryNumberColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowCreatedColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowInstallByShutdown);
		jMenuShowColumns.add(jCheckBoxMenuItemShowDepotColumn);

		return jMenuShowColumns;
	}

	private void createPdf() {
		Map<String, String> metaData = new HashMap<>();
		String title = Configed.getResourceValue("MainFrame.ClientList");

		if (mainFrame.getHostsStatusPanel().getGroupName().length() != 0) {
			title = title + ": " + mainFrame.getHostsStatusPanel().getGroupName();
		}
		metaData.put("header", title);
		title = "";
		if (mainFrame.getHostsStatusPanel().getInvolvedDepots().length() != 0) {
			title = title + "Depot(s) : " + mainFrame.getHostsStatusPanel().getInvolvedDepots();
		}

		metaData.put("title", title);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "");

		ExporterToPDF pdfExportTable = new ExporterToPDF(mainFrame.getClientTablePanel().getClientTable());

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	private void showPopupOnClientsAction() {
		JTextField durationTextField = new JTextField();
		JTextArea messageTextArea = new JTextArea();
		messageTextArea.setRows(4);

		JScrollPane messageScrollPane = new JScrollPane(messageTextArea);

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
			jMenuItem.setEnabled(countSelectedClients >= 1);
		}

		jMenuChangeClientID.setEnabled(countSelectedClients == 1);
		jMenuCopyClient.setEnabled(countSelectedClients == 1);

		List<Object> forbiddenItems = UserConfig.getCurrentUserConfig()
				.getValues(UserServerConsoleConfig.KEY_TERMINAL_ACCESS_FORBIDDEN);

		if (forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CLIENTS)
				|| !persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)
				|| persistenceController.getUserRolesConfigDataService().isGlobalReadOnly()) {
			jMenuOpenTerminalOnClient.setEnabled(false);
			jMenuOpenTerminalOnClient.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminal")
					+ Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden"));
		} else {
			jMenuOpenTerminalOnClient.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminal"));
			jMenuOpenTerminalOnClient.setEnabled(countSelectedClients == 1);
		}

		checkMenuItemsDisabling();
	}

	private void checkMenuItemsDisabling() {
		List<String> disabledClientMenuEntries = persistenceController.getConfigDataService()
				.getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {
			for (String menuActionType : disabledClientMenuEntries) {
				JMenuItem menuItem = menuItemsHost.get(menuActionType);
				Logging.debug("disable " + menuActionType + ", " + menuItem);
				if (menuItem != null) {
					menuItem.setEnabled(false);
				} else {
					Logging.warning(this, "Cannot disable menuItem", menuActionType, ", it does not exist");
				}
			}

			if (!persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD()) {
				jMenuAddClient.setEnabled(false);
				jMenuCopyClient.setEnabled(false);
			}
		}
	}

	public JPopupMenu getPopupMenuClone() {
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
				enableMenuItemsForClients();
				popupMenu.removeAll();
				cloneMenuItems(popupMenu);
			}
		});
		return popupMenu;
	}

	private void cloneMenuItems(JPopupMenu popupMenu) {
		for (int i = 0; i < jMenuClients.getItemCount(); i++) {
			Component component = jMenuClients.getMenuComponent(i);
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
