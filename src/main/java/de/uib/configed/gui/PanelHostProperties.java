/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.DefaultListCellOptions;
import de.uib.utilities.table.ListCellOptions;

public class PanelHostProperties extends JPanel {
	// delegate
	private DefaultEditMapPanel editMapPanel;
	private Map<String, Map<String, Object>> multipleMaps;

	public PanelHostProperties() {
		buildPanel();
	}

	private void buildPanel() {
		PropertiesTableCellRenderer cellRenderer = new PropertiesTableCellRenderer();
		Logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(cellRenderer, false, false);
		((EditMapPanelX) editMapPanel).setCellEditor(new SensitiveCellEditorForDataPanel());
		editMapPanel.setShowToolTip(false);

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(planeLayout.createParallelGroup().addComponent(editMapPanel))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));
		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(editMapPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void initMultipleHostsEditing(String selectedDepot, Map<String, Map<String, Object>> multipleMaps,
			UpdateCollection updateCollection, Set<String> keysOfReadOnlyEntries) {
		Logging.debug(this, "initMultipleHosts " + " configs  " + (multipleMaps)

		);

		this.multipleMaps = multipleMaps;
		editMapPanel.setUpdateCollection(updateCollection);
		editMapPanel.setReadOnlyEntries(keysOfReadOnlyEntries);

		if (selectedDepot != null && !selectedDepot.isBlank()) {
			setMap(selectedDepot);
		}
	}

	// delegated methods
	public void registerDataChangedObserver(DataChangedObserver o) {
		editMapPanel.registerDataChangedObserver(o);
	}

	private Map<String, ListCellOptions> deriveOptionsMap(Map<String, Object> m) {
		Map<String, ListCellOptions> result = new HashMap<>();

		for (Entry<String, Object> entry : m.entrySet()) {

			ListCellOptions cellOptions = null;

			if ((entry.getValue()) instanceof Boolean) {
				cellOptions = DefaultListCellOptions.getNewBooleanListCellOptions();
			} else {
				cellOptions = DefaultListCellOptions.getNewEmptyListCellOptions();
			}

			Logging.debug(this, "cellOptions: " + cellOptions);

			result.put(entry.getKey(), cellOptions);
		}
		return result;

	}

	private void setMap(String selectedItem) {
		List<Map<String, Object>> editedMaps = new ArrayList<>(1);
		editedMaps.add(multipleMaps.get(selectedItem));
		Logging.debug(this, "setMap " + multipleMaps.get(selectedItem));
		editMapPanel.setEditableMap(multipleMaps.get(selectedItem), deriveOptionsMap(multipleMaps.get(selectedItem)));
		editMapPanel.setStoreData(editedMaps);
	}
}
