package de.uib.configed.gui.productpage;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ListSelectionModel;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2013 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.guidata.InstallationStateTableModelFiltered;
import de.uib.configed.productgroup.ProductgroupPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;

public class PanelGroupedProductSettings extends PanelProductSettings {

	// State reducedTo1stSelection
	// List reductionList

	de.uib.configed.productgroup.ProductgroupPanel groupPanel;

	JMenuItemFormatted popupMarkHits;

	public PanelGroupedProductSettings(String title, ConfigedMain mainController,
			Map<String, Boolean> productDisplayFields) {
		super(title, mainController, productDisplayFields);
	}

	protected void activatePacketSelectionHandling(boolean b) {
		if (b) {
			tableProducts.getSelectionModel().addListSelectionListener(groupPanel);
		} else {
			tableProducts.getSelectionModel().removeListSelectionListener(groupPanel);
		}
	}

	public void setSearchFields(List<String> fieldList) {
		groupPanel.setSearchFields(fieldList);
	}

	@Override
	protected void initTopPane() {
		if (tableProducts == null) {
			Logging.error(this, " tableProducts == null ");
			System.exit(0);
		}
		topPane = new ProductgroupPanel(this, mainController, tableProducts);
		topPane.setVisible(true);
		groupPanel = (ProductgroupPanel) topPane;
		groupPanel.setReloadActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event reloadAction " + ae);
			reloadAction();
		});

		groupPanel.setSaveAndExecuteActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event saveAndExecuteAction " + ae);
			saveAndExecuteAction();
		});

	}

	@Override
	protected void init() {
		super.init();

		activatePacketSelectionHandling(true);
		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void setGroupsData(final Map<String, Map<String, String>> data,
			final Map<String, Set<String>> productGroupMembers) {
		groupPanel.setGroupsData(data, productGroupMembers);
		showAll();
	}

	@Override
	public void setSelection(Set<String> selectedIDs) {
		activatePacketSelectionHandling(false);
		clearSelection();
		if (selectedIDs != null) {
			if (selectedIDs.isEmpty() && tableProducts.getRowCount() > 0) {
				tableProducts.addRowSelectionInterval(0, 0);
				// show first product if no product given
				Logging.info(this, "setSelection 0");
			} else {
				for (int row = 0; row < tableProducts.getRowCount(); row++) {
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId))
						tableProducts.addRowSelectionInterval(row, row);
				}
			}
		}
		activatePacketSelectionHandling(true);
		groupPanel.findGroup(selectedIDs);
	}

	@Override
	public Set<String> getSelectedIDs() {
		Set<String> result = new HashSet<>();

		int[] selection = tableProducts.getSelectedRows();

		for (int i = 0; i < selection.length; i++) {
			result.add((String) tableProducts.getValueAt(selection[i], 0));
		}

		return result;
	}

	public void reduceToSet(Set<String> filter) {
		activatePacketSelectionHandling(false);

		InstallationStateTableModelFiltered tModel = (InstallationStateTableModelFiltered) tableProducts.getModel();
		tModel.setFilterFrom(filter);

		Logging.info(this, "reduceToSet  " + filter);
		Logging.info(this, "reduceToSet GuiIsFiltered " + groupPanel.getGuiIsFiltered());

		groupPanel.setGuiIsFiltered(filter != null && !filter.isEmpty());

		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}

	public void reduceToSelected() {
		Set<String> selection = getSelectedIDs();
		Logging.debug(this, "reduceToSelected: selectedIds  " + selection);
		reduceToSet(selection);
		setSelection(selection);
	}

	public void noSelection() {
		InstallationStateTableModelFiltered tModel = (InstallationStateTableModelFiltered) tableProducts.getModel();

		activatePacketSelectionHandling(false);
		tModel.setFilterFrom((Set<String>) null);
		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}

	public void showAll() {
		Set<String> selection = getSelectedIDs();
		noSelection();
		setSelection(selection);

	}

}
