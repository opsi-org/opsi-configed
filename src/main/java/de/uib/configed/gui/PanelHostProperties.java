/**
 * Copyright (c) UIB GmbH <info@uib.de>
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

import javax.swing.JPanel;

import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.gui.share.datapanel.EditMapPanelX;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.share.AbstractDataChangedKeeper;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

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

		this.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fill", "[]", "[]0"));
		this.add(editMapPanel, "grow");
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
	public void registerDataChangedObserver(AbstractDataChangedKeeper keeper) {
		editMapPanel.getMapTableModel().registerDataChangedKeeper(keeper);
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
