/**
 * Copyright (c) uib GmbH <info@uib.de>
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
import de.uib.configed.gui.share.table.AbstractExportTable;
import de.uib.configed.gui.share.table.ClientTableExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.HostInfo.ColumnDisplayInfo;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

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
	private JMenuItem jMenuInvertSelection = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuInvertSelection"));

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
		jMenuOpenTerminalOnClient.addActionListener(event -> TerminalController.openTerminalOnClient());

		jMenuInvertSelection.addActionListener(event -> configedMain.invertSelection());
		jMenuInvertSelection.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

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

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuInvertSelection);
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
		JMenu jMenuShowColumns = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		for (ColumnDisplayInfo info : HostInfo.ORDERED_DISPLAY_COLUMN_INFOS) {
			JCheckBoxMenuItem jCheckBoxMenuItem = createShowColumnCheckBoxMenuItem(info);
			if (jCheckBoxMenuItem != null) {
				jMenuShowColumns.add(jCheckBoxMenuItem);
			}
		}

		return jMenuShowColumns;
	}

	private JCheckBoxMenuItem createShowColumnCheckBoxMenuItem(ColumnDisplayInfo info) {
		String resourceKey = info.resourceKey;
		if (resourceKey == null) {
			Logging.warning(this, "Unknown label - not included in the menu", resourceKey);
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
			jMenuItem.setEnabled(countSelectedClients >= 1);
		}

		jMenuChangeClientID.setEnabled(countSelectedClients == 1);
		jMenuCopyClient.setEnabled(countSelectedClients == 1);

		List<Object> forbiddenItems = persistenceController.getUserRolesConfigDataService().terminalsForbidden();

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
				Logging.debug("disable ", menuActionType, ", ", menuItem);
				if (menuItem != null) {
					menuItem.setEnabled(false);
				} else {
					Logging.warning(this, "Cannot disable menuItem", menuActionType, ", it does not exist");
				}
			}

			if (!persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD()) {
				jMenuCopyClient.setEnabled(false);
			}

			if (persistenceController.getConfigDataService().getDisabledClientMenuEntries()
					.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT)) {
				jMenuAddClient.setEnabled(false);
			} else {
				jMenuAddClient.setEnabled(
						persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
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
