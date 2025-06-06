/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.permission.UserServerConsoleConfig;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.SearchTargetModel;
import de.uib.utils.table.gui.SearchTargetModelFromJList;
import de.uib.utils.table.gui.TableSearchPane;

public class DepotListPresenter extends JPanel {
	private DepotsList depotslist;
	private JScrollPane scrollpaneDepotslist;
	// this will not be shown in this panel but exported for use in other panels

	private TableSearchPane searchPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList, ConfigedMain configedMain) {
		this.configedMain = configedMain;
		this.depotslist = depotsList;

		List<String> values = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		Map<String, Map<String, Object>> depotInfo = depotsList.getDepotInfo();

		for (Entry<String, Map<String, Object>> depotInfoEntry : depotInfo.entrySet()) {
			values.add(depotInfoEntry.getKey());
			if (depotInfoEntry.getValue() == null || depotInfoEntry.getValue().get("description") == null) {
				descriptions.add("");
			} else {
				descriptions.add((String) depotInfoEntry.getValue().get("description"));
			}
		}

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(depotsList, values, descriptions);

		searchPane = new TableSearchPane(searchTargetModel);
		searchPane.setNarrow(true);

		initComponents();
		layouting();
	}

	/**
	 * exports the scrollpane which is produced in this class but displayed in
	 * other components
	 *
	 * @return a scrollpane which shows the depotslist
	 */
	public JScrollPane getScrollpaneDepotslist() {
		return scrollpaneDepotslist;
	}

	private void buildPopup() {
		JPopupMenu jPopupMenu = new JPopupMenu();
		if (persistenceController.getHostInfoCollections().getDepots().size() != 1) {
			JMenuItem selectAll = new JMenuItem(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
			selectAll.addActionListener(event -> depotslist.selectAll());
			JMenuItem selectWithEqualProperties = new JMenuItem(
					Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
			selectWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());
			jPopupMenu.add(selectAll);
			jPopupMenu.add(selectWithEqualProperties);
		}

		JMenuItem showShell = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuOpenTerminalOnDepot"));
		Icons.addIntellijIconToMenuItem(showShell, "terminal");
		jPopupMenu.add(showShell);

		jPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent event) {
				// We don't need this action here
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
				updatePopupMenuItem(showShell);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
				// We don't need this action here
			}
		});

		depotslist.setComponentPopupMenu(jPopupMenu);
	}

	/***
	 * Rebuild popup window (context menu) for depotslist This is needed to
	 * update the selected depot (e.g. open terminal on the selected depot)
	 * after the depotslist has been updated
	 */
	private void updatePopupMenuItem(JMenuItem showShell) {
		if (depotslist.getSelectedValuesList().isEmpty() || depotslist.getSelectedValuesList().size() > 1) {
			// Disable the button if no depots selected or more than one depot
			showShell.setEnabled(false);
		} else if (selectedServerForbidden()) {
			// Disable the button if the selected configserver is selected but forbidden 
			//  or if depot is selected but forbidden by config "connect.terminal.forbidden"
			showShell.setEnabled(false);
			showShell.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminalOnDepot") + " "
					+ Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden"));
		} else {
			// is allowed
			String selectedDepot = depotslist.getSelectedValue();
			if (selectedDepot != null) {
				showShell.setText(Configed.getResourceValue("MainFrame.jMenuOpenTerminalOnDepot"));
			}
			showShell.addActionListener(event -> configedMain.openTerminalOnDepot());
		}
	}

	/**
	 * Check if the selected depot/server is forbidden by the config
	 * "connect.terminal.forbidden". Called if only one depot is selected
	 * 
	 * @return true if the selected depot/server is forbidden
	 */
	private boolean selectedServerForbidden() {
		Logging.info("selectedServerForbidden.....");
		List<Object> forbiddenItems = PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().terminalsForbidden();
		Logging.info("forbiddenItems ", forbiddenItems);
		boolean forbiddenConfigServer = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER);
		boolean forbiddenConfigDepots = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_DEPOTS);

		boolean isConfigserverAndForbidden = forbiddenConfigServer && depotslist.getSelectedValuesList()
				.contains(persistenceController.getHostInfoCollections().getConfigServer());
		boolean isDepotAndForbidden = forbiddenConfigDepots && !depotslist.getSelectedValuesList().isEmpty()
				&& !depotslist.getSelectedValuesList()
						.contains(persistenceController.getHostInfoCollections().getConfigServer());

		boolean isReadOnly = PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly();

		return isConfigserverAndForbidden || isDepotAndForbidden || isReadOnly;
	}

	private void initComponents() {
		scrollpaneDepotslist = new JScrollPane();
		scrollpaneDepotslist.getViewport().add(depotslist);
		scrollpaneDepotslist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		buildPopup();
	}

	private void layouting() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(searchPane,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(scrollpaneDepotslist, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addComponent(scrollpaneDepotslist, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void selectDepotsWithEqualProperties() {
		Logging.info(this, "action on buttonSelectDepotsWithEqualProperties");

		if (depotslist.getSelectedIndex() > -1) {
			String depotSelected = depotslist.getSelectedValue();
			List<String> depotsWithEqualStock = persistenceController.getDepotDataService()
					.getAllDepotsWithIdenticalProductStock(depotSelected);
			depotslist.addToSelection(depotsWithEqualStock);
		}
	}
}
