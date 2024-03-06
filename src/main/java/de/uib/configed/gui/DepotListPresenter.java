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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TableSearchPane;
import utils.Utils;

public class DepotListPresenter extends JPanel {
	private DepotsList depotslist;
	private JScrollPane scrollpaneDepotslist;
	// this will not be shown in this panel but exported for use in other panels

	private JLabel labelDepotServer;
	private JButton buttonSelectDepotsWithEqualProperties;
	private JButton buttonSelectDepotsAll;

	private TableSearchPane searchPane;

	private boolean multidepot;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList, boolean multidepot) {
		this.depotslist = depotsList;
		this.multidepot = multidepot;

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

		searchPane = new TableSearchPane(searchTargetModel, "depotlist");
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
		labelDepotServer = new JLabel();
		if (multidepot) {
			labelDepotServer.setText(Configed.getResourceValue("DepotListPresenter.depots"));
		} else {
			labelDepotServer.setText(Configed.getResourceValue("DepotListPresenter.depot"));
		}

		buttonSelectDepotsWithEqualProperties = new JButton(Utils.createImageIcon("images/equalplus.png", ""));
		buttonSelectDepotsWithEqualProperties
				.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		buttonSelectDepotsWithEqualProperties.setFocusable(false);
		buttonSelectDepotsWithEqualProperties.addActionListener(event -> selectDepotsWithEqualProperties());
		buttonSelectDepotsWithEqualProperties.setEnabled(multidepot);

		buttonSelectDepotsAll = new JButton(Utils.createImageIcon("images/plusplus.png", ""));
		buttonSelectDepotsAll.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
		buttonSelectDepotsAll.setFocusable(false);
		buttonSelectDepotsAll.addActionListener(event -> depotslist.selectAll());
		buttonSelectDepotsAll.setEnabled(multidepot);

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
	}

	private void layouting() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelDepotServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsWithEqualProperties, GroupLayout.Alignment.TRAILING,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsAll, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(searchPane,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(scrollpaneDepotslist, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelDepotServer, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(buttonSelectDepotsWithEqualProperties, Globals.SQUARE_BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsAll, Globals.SQUARE_BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE))
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
