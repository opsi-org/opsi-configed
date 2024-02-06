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
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditObject;
import de.uib.utilities.swing.FEditTextWithExtra;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.ClientTableExporterToCSV;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import utils.Utils;

@SuppressWarnings({ "java:S1200" })
public class ClientMenu extends JMenu {
	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENSES = "free licenses for client";

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private JMenuItem jMenuRemoteControl = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
	private JMenuItem jMenuShowPopupMessage = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
	private JMenuItem jMenuRequestSessionInfo = new JMenuItem(
			Configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
	private JMenuItem jMenuShutdownClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuShutdownClient"));
	private JMenuItem jMenuRebootClient = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuRebootClient"));
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
	private JMenuItem jMenuRebuildClientList = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.reload"),
			Utils.createImageIcon("images/reload16.png", ""));

	private JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProducts, jMenuDeleteClient,
			jMenuFreeLicenses, jMenuShowPopupMessage, jMenuRequestSessionInfo, jMenuDeletePackageCaches,
			jMenuRebootClient, jMenuShutdownClient, jMenuChangeDepot, jMenuRemoteControl };

	private Map<String, JMenuItem> menuItemsHost;
	private ConfigedMain configedMain;
	private MainFrame mainFrame;

	public ClientMenu(MainFrame mainFrame, ConfigedMain configedMain) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		initJMenu();
	}

	@SuppressWarnings({ "java:S138" })
	private void initJMenu() {
		setText(Configed.getResourceValue("MainFrame.jMenuClients"));

		addMenuListener(new MenuListener() {
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
		});

		jMenuChangeDepot.addActionListener((ActionEvent e) -> configedMain.callChangeDepotDialog());
		jMenuChangeClientID.addActionListener((ActionEvent e) -> configedMain.callChangeClientIDDialog());

		JMenuItem jMenuSelectionGetGroup = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuSelectionGetGroup.addActionListener((ActionEvent e) -> mainFrame.callSelectionDialog());

		JMenuItem jMenuSelectionGetSavedSearch = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuSelectionGetSavedSearch.addActionListener((ActionEvent e) -> configedMain.clientSelectionGetSavedSearch());

		jMenuRebuildClientList.addActionListener((ActionEvent e) -> configedMain.reloadHosts());
		jMenuClientSelectionToggleFilter.setState(false);
		jMenuClientSelectionToggleFilter.addActionListener((ActionEvent e) -> mainFrame.toggleClientFilterAction());

		JMenuItem jMenuCreatePdf = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"),
				Utils.createImageIcon("images/acrobat_reader16.png", ""));
		jMenuCreatePdf.addActionListener((ActionEvent e) -> createPdf());

		jMenuAddClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		menuItemsHost = new LinkedHashMap<>();
		menuItemsHost.put(ITEM_ADD_CLIENT, jMenuAddClient);
		menuItemsHost.put(ITEM_DELETE_CLIENT, jMenuDeleteClient);
		menuItemsHost.put(ITEM_FREE_LICENSES, jMenuFreeLicenses);

		jMenuDeletePackageCaches
				.addActionListener((ActionEvent e) -> configedMain.deletePackageCachesOfSelectedClients());

		JMenu jMenuOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.addActionListener((ActionEvent e) -> configedMain.fireOpsiclientdEventOnSelectedClients(event));
			jMenuOpsiClientdEvent.add(item);
		}

		jMenuShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());
		jMenuShutdownClient.addActionListener((ActionEvent e) -> configedMain.shutdownSelectedClients());
		jMenuRequestSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			configedMain.getSessionInfo();
		});
		jMenuRebootClient.addActionListener((ActionEvent e) -> configedMain.rebootSelectedClients());
		jMenuDeleteClient.addActionListener((ActionEvent e) -> configedMain.deleteSelectedClients());
		jMenuCopyClient.addActionListener((ActionEvent e) -> configedMain.copySelectedClient());
		jMenuFreeLicenses
				.addActionListener((ActionEvent e) -> configedMain.freeAllPossibleLicensesForSelectedClients());
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		jMenuRemoteControl.addActionListener(
				(ActionEvent e) -> mainFrame.getClientTable().startRemoteControlForSelectedClients());

		add(initWakeOnLANMenu());
		add(jMenuOpsiClientdEvent);
		add(jMenuShowPopupMessage);
		add(jMenuRequestSessionInfo);
		add(jMenuDeletePackageCaches);

		addSeparator();

		add(jMenuShutdownClient);
		add(jMenuRebootClient);
		add(jMenuRemoteControl);

		addSeparator();

		add(jMenuAddClient);
		if (ServerFacade.isOpsi43()) {
			add(jMenuCopyClient);
		}
		add(jMenuDeleteClient);

		add(initResetProductsMenu());

		add(jMenuFreeLicenses);
		add(jMenuChangeClientID);
		if (mainFrame.isMultiDepot()) {
			add(jMenuChangeDepot);
		}
		addSeparator();

		add(jMenuSelectionGetGroup);
		add(jMenuSelectionGetSavedSearch);

		addSeparator();

		add(jMenuClientSelectionToggleFilter);

		add(jMenuRebuildClientList);
		add(jMenuCreatePdf);

		AbstractExportTable exportTable = new ExporterToCSV(mainFrame.getClientTable().getTable());
		exportTable.addMenuItemsTo(this);

		ClientTableExporterToCSV clientTableExporter = new ClientTableExporterToCSV(
				mainFrame.getClientTable().getTable());
		clientTableExporter.addMenuItemsTo(this);

		addSeparator();

		add(initShowColumnsMenu());
	}

	private JMenu initWakeOnLANMenu() {
		JMenu jMenuWakeOnLan = new JMenu(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
		JMenuItem jMenuDirectWOL = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		jMenuDirectWOL.addActionListener((ActionEvent e) -> configedMain.wakeSelectedClients());
		jMenuWakeOnLan.add(jMenuDirectWOL);

		Map<String, Integer> labelledDelays = new LinkedHashMap<>();
		labelledDelays.put("0 sec", 0);
		labelledDelays.put("5 sec", 5);
		labelledDelays.put("20 sec", 20);
		labelledDelays.put("1 min", 60);
		labelledDelays.put("2 min", 120);
		labelledDelays.put("10 min", 600);
		labelledDelays.put("20 min", 1200);
		labelledDelays.put("1 h", 3600);

		JMenuItem jMenuNewScheduledWOL = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		jMenuNewScheduledWOL.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(Configed.getResourceValue("FStartWakeOnLan.title"),
					configedMain);
			fStartWakeOnLan.setLocationRelativeTo(mainFrame);
			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);
			fStartWakeOnLan.setClients();
		});

		JMenuItem jMenuShowScheduledWOL = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning"));
		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.addActionListener(
				(ActionEvent e) -> executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll()));

		jMenuWakeOnLan.add(jMenuNewScheduledWOL);
		jMenuWakeOnLan.addSeparator();
		jMenuWakeOnLan.add(jMenuShowScheduledWOL);

		return jMenuWakeOnLan;
	}

	private JMenu initResetProductsMenu() {
		JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));
		jMenuResetProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, true));

		JMenuItem jMenuResetProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));
		jMenuResetProductOnClient.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, true));

		JMenuItem jMenuResetLocalbootProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithStates"));
		jMenuResetLocalbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, false));

		JMenuItem jMenuResetLocalbootProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithoutStates"));
		jMenuResetLocalbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, false));

		JMenuItem jMenuResetNetbootProductOnClientWithStates = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithStates"));
		jMenuResetNetbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, false, true));

		JMenuItem jMenuResetNetbootProductOnClient = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithoutStates"));
		jMenuResetNetbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, false, true));

		jMenuResetProducts.add(jMenuResetLocalbootProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetLocalbootProductOnClient);
		jMenuResetProducts.add(jMenuResetNetbootProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetNetbootProductOnClient);
		jMenuResetProducts.add(jMenuResetProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetProductOnClient);

		return jMenuResetProducts;
	}

	@SuppressWarnings({ "java:S138" })
	private JMenu initShowColumnsMenu() {
		JCheckBoxMenuItem jCheckBoxMenuItemShowCreatedColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		jCheckBoxMenuItemShowCreatedColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowCreatedColumn
				.addActionListener((ActionEvent e) -> configedMain.toggleColumn(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowWANactiveColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		jCheckBoxMenuItemShowWANactiveColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowWANactiveColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowIPAddressColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		jCheckBoxMenuItemShowIPAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowIPAddressColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowSystemUUIDColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowSystemUUIDColumn"));
		jCheckBoxMenuItemShowSystemUUIDColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSystemUUIDColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowHardwareAddressColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		jCheckBoxMenuItemShowHardwareAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowHardwareAddressColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowSessionInfoColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		jCheckBoxMenuItemShowSessionInfoColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSessionInfoColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowInventoryNumberColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		jCheckBoxMenuItemShowInventoryNumberColumn.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInventoryNumberColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowUefiBoot = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		jCheckBoxMenuItemShowUefiBoot
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowUefiBoot.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowInstallByShutdown = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		jCheckBoxMenuItemShowInstallByShutdown.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInstallByShutdown.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		JCheckBoxMenuItem jCheckBoxMenuItemShowDepotColumn = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		jCheckBoxMenuItemShowDepotColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowDepotColumn.addActionListener(
				(ActionEvent e) -> configedMain.toggleColumn(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

		JMenu jMenuShowColumns = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		jMenuShowColumns.add(jCheckBoxMenuItemShowWANactiveColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowIPAddressColumn);

		if (ServerFacade.isOpsi43()) {
			jMenuShowColumns.add(jCheckBoxMenuItemShowSystemUUIDColumn);
		}

		jMenuShowColumns.add(jCheckBoxMenuItemShowHardwareAddressColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowSessionInfoColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowInventoryNumberColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowCreatedColumn);

		if (!ServerFacade.isOpsi43()) {
			jMenuShowColumns.add(jCheckBoxMenuItemShowUefiBoot);
		}

		jMenuShowColumns.add(jCheckBoxMenuItemShowInstallByShutdown);
		jMenuShowColumns.add(jCheckBoxMenuItemShowDepotColumn);

		return jMenuShowColumns;
	}

	private void createPdf() {
		TableModel tm = configedMain.getSelectedClientsTableModel();
		JTable jTable = new JTable(tm);

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
		pdfExportTable.execute(null, jTable.getSelectedRowCount() != 0);
	}

	private void showPopupOnClientsAction() {
		FEditTextWithExtra fText = new FEditTextWithExtra("", Configed.getResourceValue("MainFrame.writePopupMessage"),
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

	private void executeCommandOnInstances(String command, Set<JDialog> instances) {
		Logging.info("executeCommandOnInstances " + command + " for count instances " + instances.size());
		if ("arrange".equals(command)) {
			arrangeWs(instances);
		}
	}

	private void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null) {
			return;
		}

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

		checkMenuItemsDisabling();
	}

	private void checkMenuItemsDisabling() {
		if (menuItemsHost == null) {
			Logging.info("checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}

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
		for (int i = 0; i < getItemCount(); i++) {
			Component component = getMenuComponent(i);
			if (component instanceof JSeparator) {
				popupMenu.addSeparator();
			}

			if (component instanceof JMenuItem) {
				JMenuItem sourceItem = (JMenuItem) component;
				JMenuItem clonedItem = cloneMenuItem(sourceItem);
				popupMenu.add(clonedItem);
			}
		}
	}

	private static JMenuItem cloneMenuItem(JMenuItem sourceItem) {
		JMenuItem clonedItem;
		if (sourceItem instanceof JMenu) {
			clonedItem = new JMenu(sourceItem.getText());
			JMenu sourceSubMenu = (JMenu) sourceItem;
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
			if (sourceItem.getIcon() != null) {
				clonedItem = new JMenuItem(sourceItem.getText(), sourceItem.getIcon());
			} else {
				clonedItem = new JMenuItem(sourceItem.getText());
			}
			clonedItem.setAccelerator(sourceItem.getAccelerator());
			clonedItem.setEnabled(sourceItem.isEnabled());
		}
		for (ActionListener listener : sourceItem.getActionListeners()) {
			clonedItem.addActionListener(listener);
		}

		return clonedItem;
	}

	public void toggleClientFilterAction(boolean rebuildClientListTableModel) {
		configedMain.toggleFilterClientList(rebuildClientListTableModel);
		jMenuClientSelectionToggleFilter.setState(configedMain.isFilterClientList());
	}
}
