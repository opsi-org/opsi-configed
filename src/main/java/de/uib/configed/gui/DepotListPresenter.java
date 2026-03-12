/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.core.domain.permission.UserServerConsoleConfig;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.terminal.TerminalController;
import de.uib.configed.gui.share.table.gui.SearchTargetModel;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromJList;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class DepotListPresenter extends JPanel {
	private DepotsList depotslist;
	private JScrollPane scrollpaneDepotslist;
	// this will not be shown in this panel but exported for use in other panels

	private TableSearchPane searchPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList) {
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
		searchPane.setFiltering();
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
		JMenuItem selectWithEqualProperties = new JMenuItem(
				Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		JMenuItem selectAll = new JMenuItem(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));

		if (persistenceController.getDataServices().hostInfoCollections.getDepots().size() != 1) {
			selectAll.addActionListener(event -> depotslist.selectAll());
			selectWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());
			jPopupMenu.add(selectAll);
			jPopupMenu.add(selectWithEqualProperties);
		}

		JMenuItem showShell = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuOpenTerminalOnDepot"));
		Icons.addIntellijIconToMenuItem(showShell, "terminal");
		jPopupMenu.add(showShell);

		depotslist.addMouseListener(new PopupMouseListener(jPopupMenu,
				e -> updatePopupMenuItem(showShell, selectWithEqualProperties, selectAll)));

	}

	/***
	 * Rebuild popup window (context menu) for depotslist This is needed to
	 * update the selected depot (e.g. open terminal on the selected depot)
	 * after the depotslist has been updated
	 */
	private boolean updatePopupMenuItem(JMenuItem showShell, JMenuItem selectWithEqualProperties, JMenuItem selectAll) {
		if (depotslist.getSelectedValuesList().size() != 1) {
			if (depotslist.getSelectedValuesList().size() == depotslist.getModel().getSize()) {
				selectAll.setEnabled(false);
			}

			// Disable the button if no depots selected or more than one depot
			showShell.setEnabled(false);

			selectWithEqualProperties.setEnabled(false);
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
			showShell.addActionListener(event -> TerminalController.openTerminalOnDepot());
			showShell.setEnabled(true);

			selectWithEqualProperties.setEnabled(true);

			selectAll.setEnabled(true);
		}

		return true;
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
				.getDataServices().userRoles.terminalsForbidden();
		Logging.info("forbiddenItems ", forbiddenItems);
		boolean forbiddenConfigServer = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER);
		boolean forbiddenConfigDepots = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_DEPOTS);

		boolean isConfigserverAndForbidden = forbiddenConfigServer && depotslist.getSelectedValuesList()
				.contains(persistenceController.getDataServices().hostInfoCollections.getConfigServer());
		boolean isDepotAndForbidden = forbiddenConfigDepots && !depotslist.getSelectedValuesList().isEmpty()
				&& !depotslist.getSelectedValuesList()
						.contains(persistenceController.getDataServices().hostInfoCollections.getConfigServer());

		boolean isReadOnly = PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
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
		this.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fillx, wrap 1", "[grow, fill]",
				"[]" + Globals.MIN_GAP_SIZE + "[grow, fill]"));

		add(searchPane);
		add(scrollpaneDepotslist, "grow, push");
	}

	private void selectDepotsWithEqualProperties() {
		Logging.info(this, "action on buttonSelectDepotsWithEqualProperties");

		if (depotslist.getSelectedIndex() > -1) {
			String depotSelected = depotslist.getSelectedValue();
			List<String> depotsWithEqualStock = persistenceController.getDataServices().depot
					.getAllDepotsWithIdenticalProductStock(depotSelected);
			depotslist.addToSelection(depotsWithEqualStock);
		}
	}
}
