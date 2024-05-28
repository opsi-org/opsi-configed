/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
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

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.SearchTargetModel;
import de.uib.utils.table.gui.SearchTargetModelFromJList;
import de.uib.utils.table.gui.TableSearchPane;

public class DepotListPresenter extends JPanel {
	private DepotsList depotslist;
	private JScrollPane scrollpaneDepotslist;
	// this will not be shown in this panel but exported for use in other panels

	private TableSearchPane searchPane;

	private boolean multidepot;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList) {
		this.depotslist = depotsList;
		this.multidepot = persistenceController.getHostInfoCollections().getDepots().size() != 1;

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
		searchPane.setSearchMode(TableSearchPane.SearchMode.FULL_TEXT_SEARCH);
		searchPane.setSearchFields(new Integer[] { 0, 1 });

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

	private void initComponents() {
		if (!multidepot) {
			searchPane.setEnabled(false);
		}

		searchPane.setNarrow(true);

		// not visible in this panel

		depotslist.setMaximumSize(new Dimension(200, 400));

		scrollpaneDepotslist = new JScrollPane();
		scrollpaneDepotslist.getViewport().add(depotslist);
		scrollpaneDepotslist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneDepotslist.setPreferredSize(depotslist.getMaximumSize());

		if (multidepot) {
			JPopupMenu jPopupMenu = new JPopupMenu();
			JMenuItem selectAll = new JMenuItem(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
			selectAll.addActionListener(event -> depotslist.selectAll());

			JMenuItem selectWithEqualProperties = new JMenuItem(
					Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
			selectWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());

			jPopupMenu.add(selectAll);
			jPopupMenu.add(selectWithEqualProperties);

			depotslist.setComponentPopupMenu(jPopupMenu);
		}
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
