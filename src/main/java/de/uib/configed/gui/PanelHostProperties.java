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
import de.uib.utils.DataChangedObserver;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.DefaultListCellOptions;
import de.uib.utils.table.ListCellOptions;

public class PanelHostProperties extends JPanel {
	// delegate
	private EditMapPanelX editMapPanel;
	private Map<String, Map<String, Object>> multipleMaps;

	public PanelHostProperties() {
		buildPanel();
	}

	private void buildPanel() {
		Logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);
		editMapPanel.setCellEditor(new SensitiveCellEditorForDataPanel());
		editMapPanel.setShowToolTip(false);

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createSequentialGroup().addComponent(editMapPanel));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(editMapPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void initMultipleHostsEditing(String selectedDepot, Map<String, Map<String, Object>> multipleMaps,
			UpdateCollection updateCollection, Set<String> keysOfReadOnlyEntries) {
		Logging.debug(this, "initMultipleHosts " + " configs  " + (multipleMaps));

		this.multipleMaps = multipleMaps;
		editMapPanel.setUpdateCollection(updateCollection);
		editMapPanel.setReadOnlyEntries(keysOfReadOnlyEntries);

		setMap(selectedDepot);
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

	private void setMap(String selectedDepot) {
		if (selectedDepot == null || selectedDepot.isBlank()) {
			editMapPanel.setEditableMap(null, null);
		} else {
			List<Map<String, Object>> editedMaps = new ArrayList<>(1);
			editedMaps.add(multipleMaps.get(selectedDepot));
			Logging.debug(this, "setMap " + multipleMaps.get(selectedDepot));
			editMapPanel.setEditableMap(multipleMaps.get(selectedDepot),
					deriveOptionsMap(multipleMaps.get(selectedDepot)));
			editMapPanel.setStoreData(editedMaps);
		}
	}
}
