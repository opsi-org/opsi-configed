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
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utils.DataChangedObserver;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.logging.Logging;

public class PanelHostProperties extends JPanel {
	// delegate
	private EditMapPanelX editMapPanel;
	private Map<String, Map<String, Object>> multipleMaps;

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

	public void initMultipleHostsEditing(String selectedDepot, Map<String, Map<String, Object>> multipleMaps,
			UpdateCollection updateCollection, Set<String> keysOfReadOnlyEntries) {
		Logging.debug(this, "initMultipleHosts ", " configs  ", multipleMaps);

		this.multipleMaps = multipleMaps;
		editMapPanel.setUpdateCollection(POJOReMapper.remap(updateCollection));
		editMapPanel.getMapTableModel().setReadOnlyEntries(keysOfReadOnlyEntries);

		setMap(selectedDepot);
	}

	// delegated methods
	public void registerDataChangedObserver(DataChangedObserver o) {
		editMapPanel.getMapTableModel().registerDataChangedObserver(o);
	}

	private Map<String, ConfigOption> deriveOptionsMap(Map<String, Object> m) {
		Map<String, ConfigOption> result = new HashMap<>();

		for (Entry<String, Object> entry : m.entrySet()) {
			ConfigOption cellOptions;

			if ((entry.getValue()) instanceof Boolean) {
				cellOptions = ConfigOption.createConfigOption("", TYPE.BOOL_CONFIG, false, false);
			} else {
				cellOptions = ConfigOption.createConfigOption("", TYPE.UNICODE_CONFIG, true, false);
			}

			Logging.debug(this, "cellOptions: ", cellOptions);

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
			Logging.debug(this, "setMap ", multipleMaps.get(selectedDepot));
			editMapPanel.setEditableMap(multipleMaps.get(selectedDepot),
					deriveOptionsMap(multipleMaps.get(selectedDepot)));
			editMapPanel.getMapTableModel().setStoreData(editedMaps);
		}
	}
}
