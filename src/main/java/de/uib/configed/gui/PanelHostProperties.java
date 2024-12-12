/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utils.DataChangedObserver;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.logging.Logging;

public class PanelHostProperties extends JPanel {
	// delegate
	private EditMapPanelX editMapPanel;

	public PanelHostProperties() {
		buildPanel();
	}

	private void buildPanel() {
		Logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(false, false, false);
		editMapPanel.setShowToolTip(false);

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createSequentialGroup().addComponent(editMapPanel));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(editMapPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void initMultipleHostsEditing(Map<String, Object> depotMap, UpdateCollection updateCollection,
			Set<String> keysOfReadOnlyEntries) {
		Logging.debug(this, "initMultipleHosts ", " configs  ", depotMap);

		editMapPanel.getMapTableModel().setReadOnlyEntries(keysOfReadOnlyEntries);

		Logging.debug(this, "derive Map ", depotMap);

		deriveDepotMap(depotMap);
		editMapPanel.setEditableMap(depotMap, deriveOptionsMap(depotMap));
		editMapPanel.updateData(updateCollection, Collections.singletonList(depotMap));

		editMapPanel.getMapTableModel().setReadOnlyEntries(keysOfReadOnlyEntries);
	}

	// delegated methods
	public void registerDataChangedObserver(DataChangedObserver o) {
		editMapPanel.getMapTableModel().registerDataChangedObserver(o);
	}

	private Map<String, ConfigOption> deriveOptionsMap(Map<String, Object> depotMap) {
		Map<String, ConfigOption> result = new HashMap<>();

		for (Entry<String, Object> entry : depotMap.entrySet()) {
			ConfigOption cellOptions;

			if (((List<?>) entry.getValue()).get(0) instanceof Boolean) {
				cellOptions = ConfigOption.createConfigOption("", TYPE.BOOL_CONFIG, false, false);
			} else {
				cellOptions = ConfigOption.createConfigOption("", TYPE.UNICODE_CONFIG, true, false);
			}

			Logging.debug(this, "cellOptions: ", cellOptions);

			result.put(entry.getKey(), cellOptions);
		}
		return result;
	}

	private Map<String, Object> deriveDepotMap(Map<String, Object> depotMap) {
		Logging.debug(this, "deriveDepotMap  ", depotMap);
		for (Entry<String, Object> dataEntry : depotMap.entrySet()) {
			if (!(dataEntry.getValue() instanceof List)) {
				depotMap.put(dataEntry.getKey(), Collections.singletonList(dataEntry.getValue()));
			}
		}

		return depotMap;
	}
}
