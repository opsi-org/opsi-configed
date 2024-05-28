/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FEditObject;
import de.uib.utils.swing.FEditText;
import de.uib.utils.table.AbstractExportTable;
import de.uib.utils.table.ClientTableExporterToCSV;
import de.uib.utils.table.ExporterToCSV;
import de.uib.utils.table.ExporterToPDF;

@SuppressWarnings({ "java:S1200" })
public final class ClientMenuManager implements MenuListener {
	private static ClientMenuManager instance;

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
	private JMenu jMenuResetProducts = new JMenu(Configed.getResourceValue("MainFrame.jMenuResetProducts"));
	private JMenuItem jMenuFreeLicenses = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFreeLicenses"));
	private JMenuItem jMenuDeletePackageCaches = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
	private JCheckBoxMenuItem jMenuClientSelectionToggleFilter = new JCheckBoxMenuItem(
			Configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));

	private JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProducts, jMenuDeleteClient,
			jMenuFreeLicenses, jMenuShowPopupMessage, jMenuRequestSessionInfo, jMenuDeletePackageCaches,
			jMenuRebootClient, jMenuShutdownClient, jMenuChangeDepot, jMenuRemoteControl };

	private JMenu jMenu = new JMenu(Configed.getResourceValue("MainFrame.jMenuClients"));

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
		instance = new ClientMenuManager(configedMain, mainFrame);
		return instance;
	}

	public static ClientMenuManager getInstance() {
		return instance;
	}

	public JCheckBoxMenuItem getClientSelectionToggleFilterMenu() {
		return jMenuClientSelectionToggleFilter;
	}

	public JMenu getJMenu() {
		return jMenu;
	}

	private void initJMenu() {
		jMenu.addMenuListener(this);

		jMenuChangeDepot.addActionListener(event -> configedMain.callChangeDepotDialog());
		jMenuChangeClientID.addActionListener(event -> configedMain.callChangeClientIDDialog());

		JMenuItem jMenuSelectionGetGroup = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuSelectionGetGroup.addActionListener(event -> configedMain.callClientSelectionDialog());

		JMenuItem jMenuSelectionGetSavedSearch = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuSelectionGetSavedSearch.addActionListener(event -> configedMain.clientSelectionGetSavedSearch());

		JMenuItem jMenuRebuildClientList = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.reload"),
				Utils.getIntellijIcon("refresh"));
		jMenuRebuildClientList.addActionListener(event -> configedMain.reloadHosts());
		jMenuClientSelectionToggleFilter.setState(false);
		jMenuClientSelectionToggleFilter.addActionListener(event -> mainFrame.toggleClientFilterAction());

		JMenuItem jMenuCreatePdf = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"),
				Utils.createImageIcon("images/acrobat_reader16.png", ""));
		jMenuCreatePdf.addActionListener(event -> createPdf());

		jMenuAddClient.addActionListener(event -> configedMain.callNewClientDialog());

		jMenuDeletePackageCaches.addActionListener(event -> configedMain.deletePackageCachesOfSelectedClients());

		JMenuItem jMenuWakeOnLan = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
		jMenuWakeOnLan.addActionListener(event -> configedMain.wakeSelectedClients());

		JMenu jMenuOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.addActionListener((ActionEvent e) -> configedMain.fireOpsiclientdEventOnSelectedClients(event));
			jMenuOpsiClientdEvent.add(item);
		}

		jMenuShowPopupMessage.addActionListener(event -> showPopupOnClientsAction());
		jMenuShutdownClient.addActionListener(event -> configedMain.shutdownSelectedClients());
		jMenuRequestSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			configedMain.getSessionInfo();
		});
		jMenuRebootClient.addActionListener(event -> configedMain.rebootSelectedClients());
		jMenuDeleteClient.addActionListener(event -> configedMain.deleteSelectedClients());
		jMenuCopyClient.addActionListener(event -> configedMain.copySelectedClient());
		jMenuFreeLicenses.addActionListener(event -> configedMain.freeAllPossibleLicensesForSelectedClients());
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		jMenuRemoteControl
				.addActionListener(event -> mainFrame.getClientTable().startRemoteControlForSelectedClients());
		jMenuOpenTerminalOnClient.addActionListener(event -> configedMain.openTerminalOnClient());

		jMenu.add(jMenuWakeOnLan);
		jMenu.add(jMenuOpsiClientdEvent);
		jMenu.add(jMenuShowPopupMessage);
		jMenu.add(jMenuRequestSessionInfo);
		jMenu.add(jMenuDeletePackageCaches);

		jMenu.addSeparator();

		jMenu.add(jMenuShutdownClient);
		jMenu.add(jMenuRebootClient);
		jMenu.add(jMenuOpenTerminalOnClient);
		jMenu.add(jMenuRemoteControl);

		jMenu.addSeparator();

		jMenu.add(jMenuAddClient);
		jMenu.add(jMenuCopyClient);
		jMenu.add(jMenuDeleteClient);

		jMenu.add(initResetProductsMenu());

		jMenu.add(jMenuFreeLicenses);
		jMenu.add(jMenuChangeClientID);

		// is multiDepot
		if (persistenceController.getHostInfoCollections().getDepots().size() != 1) {
			jMenu.add(jMenuChangeDepot);
		}
		jMenu.addSeparator();

		jMenu.add(jMenuSelectionGetGroup);
		jMenu.add(jMenuSelectionGetSavedSearch);

		jMenu.addSeparator();

		jMenu.add(jMenuClientSelectionToggleFilter);

		jMenu.add(jMenuRebuildClientList);
		jMenu.add(jMenuCreatePdf);

		AbstractExportTable exportTable = new ExporterToCSV(mainFrame.getClientTable().getTable());
		exportTable.addMenuItemsTo(jMenu);

		ClientTableExporterToCSV clientTableExporter = new ClientTableExporterToCSV(
				mainFrame.getClientTable().getTable());
		clientTableExporter.addMenuItemsTo(jMenu);

		jMenu.addSeparator();

		jMenu.add(initShowColumnsMenu());
	}

	private JMenu initResetProductsMenu() {
		addResetProductsMenuItemsTo(jMenuResetProducts);
		return jMenuResetProducts;
	}

	public void addResetProductsMenuItemsTo(JMenu jMenu) {
		addResetProductsMenuItemsTo(jMenu, true, true, true);
	}

	public void addResetLocalbootProductsMenuItemsTo(JMenu jMenu) {
		addResetProductsMenuItemsTo(jMenu, true, false, false);
	}

	public void addResetNetbootProductsMenuItemsTo(JMenu jMenu) {
		addResetProductsMenuItemsTo(jMenu, false, true, false);
	}

	public static JMenuItem createArrangeWindowsMenuItem() {
		JMenuItem jMenuShowScheduledWOL = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning"));
		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.addActionListener(event -> arrangeWs(FEditObject.runningInstances.getAll()));

		return jMenuShowScheduledWOL;
	}

	private void addResetProductsMenuItemsTo(JMenu jMenu, boolean includeResetOptionForLocalbootProducts,
			boolean includeResetOptionForNetbootProducts, boolean includeResetOptionForBothProducts) {
		JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));
		jMenuResetProductOnClientWithStates.addActionListener(event -> resetProductOnClientAction(true, true, true));

		JMenuItem jMenuResetProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));
		jMenuResetProductOnClient.addActionListener(event -> resetProductOnClientAction(false, true, true));

		JMenuItem jMenuResetLocalbootProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithStates"));
		jMenuResetLocalbootProductOnClientWithStates
				.addActionListener(event -> resetProductOnClientAction(true, true, false));

		JMenuItem jMenuResetLocalbootProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithoutStates"));
		jMenuResetLocalbootProductOnClient.addActionListener(event -> resetProductOnClientAction(false, true, false));

		JMenuItem jMenuResetNetbootProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithStates"));
		jMenuResetNetbootProductOnClientWithStates
				.addActionListener(event -> resetProductOnClientAction(true, false, true));

		JMenuItem jMenuResetNetbootProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithoutStates"));
		jMenuResetNetbootProductOnClient.addActionListener(event -> resetProductOnClientAction(false, false, true));

		if (includeResetOptionForLocalbootProducts) {
			jMenu.add(jMenuResetLocalbootProductOnClientWithStates);
			jMenu.add(jMenuResetLocalbootProductOnClient);
		}
		if (includeResetOptionForNetbootProducts) {
			jMenu.add(jMenuResetNetbootProductOnClientWithStates);
			jMenu.add(jMenuResetNetbootProductOnClient);
		}
		if (includeResetOptionForBothProducts) {
			jMenu.add(jMenuResetProductOnClientWithStates);
			jMenu.add(jMenuResetProductOnClient);
		}
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
		jMenuShowColumns.setIcon(Utils.getIntellijIcon("inspectionsEye"));

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

		ExporterToPDF pdfExportTable = new ExporterToPDF(mainFrame.getClientTable().getTable());

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	private void showPopupOnClientsAction() {
		FEditText fText = new FEditText("", Configed.getResourceValue("MainFrame.writePopupMessage"),
				Configed.getResourceValue("MainFrame.writePopupDuration")) {
			@Override
			protected void commit() {
				super.commit();
				Float duration = 0F;
				if (!getExtra().isEmpty()) {
					duration = Float.parseFloat(getExtra());
				}
				configedMain.showPopupOnSelectedClients(getText(), duration);
			}
		};

		fText.setTitle(Configed.getResourceValue("MainFrame.popupFrameTitle"));
		fText.init();
		fText.setLocationRelativeTo(mainFrame);
		fText.setVisible(true);
	}

	private static void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null) {
			return;
		}

		MainFrame mainFrame = ConfigedMain.getMainFrame();
		int transpose = 20;

		for (Window f : frames) {
			transpose = transpose + Globals.LINE_HEIGHT;

			if (f != null) {
				f.setVisible(true);
				f.setLocation(mainFrame.getLocation().x + transpose, mainFrame.getLocation().y + transpose);
			}
		}
	}

	private void resetProductOnClientAction(boolean withProductProperties, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		configedMain.resetProductsForSelectedClients(withProductProperties, resetLocalbootProducts,
				resetNetbootProducts);
	}

	private void enableMenuItemsForClients() {
		int countSelectedClients = configedMain.getSelectedClients().size();
		Logging.debug(" enableMenuItemsForClients, countSelectedClients " + countSelectedClients);

		for (JMenuItem jMenuItem : clientMenuItemsDependOnSelectionCount) {
			jMenuItem.setEnabled(countSelectedClients >= 1);
		}

		jMenuChangeClientID.setEnabled(countSelectedClients == 1);
		jMenuCopyClient.setEnabled(countSelectedClients == 1);
		jMenuOpenTerminalOnClient.setEnabled(countSelectedClients == 1);

		checkMenuItemsDisabling();
	}

	private void checkMenuItemsDisabling() {
		List<String> disabledClientMenuEntries = persistenceController.getConfigDataService()
				.getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {
			for (String menuActionType : disabledClientMenuEntries) {
				JMenuItem menuItem = menuItemsHost.get(menuActionType);
				Logging.debug("disable " + menuActionType + ", " + menuItem);
				menuItem.setEnabled(false);
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
		for (int i = 0; i < jMenu.getItemCount(); i++) {
			Component component = jMenu.getMenuComponent(i);
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
			clonedItem.setIcon(sourceItem.getIcon());
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
