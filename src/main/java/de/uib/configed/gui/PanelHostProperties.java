/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.swing.JPopupMenu;

import de.uib.configed.core.domain.datachanges.HostUpdateCollection;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.share.datapanel.EditMapPanelX;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.share.logging.Logging;

public class PanelHostProperties extends AbstractConfigurationTab {
	private EditMapPanelX editMapPanel;

	private HostUpdateCollection hostUpdateCollection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Supplier<String> depotSupplier;

	public PanelHostProperties(Supplier<String> depotSupplier) {
		super(false, false);
		this.depotSupplier = depotSupplier;

		buildPanel();
	}

	private void buildPanel() {
		Logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(false, false) {
			@Override
			protected JPopupMenu createBasicPopup() {
				return new PopupMenuTrait(List.of(PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD),
						event -> updatePopupMenu(), List.of(table, jScrollPane.getViewport())) {
					@Override
					public void action(int p) {
						super.action(p);
						handlePopupMenuActions(p);
					}
				};
			}
		};
		editMapPanel.getMapTableModel().registerDataChangedKeeper(ChangedDataManager.getGeneralDataChangedKeeper());
		editMapPanel.setShowToolTip(false);

		setComponent(editMapPanel);
	}

	private void handlePopupMenuActions(int p) {
		if (p == PopupMenuTrait.POPUP_RELOAD) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			if (!CacheIdentifier.ALL_DATA.toString().equals(persistenceController.getTriggeredEvent())) {
				persistenceController.reloadData(ReloadEvent.DEPOT_PROPERTIES_DATA_RELOAD.toString());
			}

			updateContent();

			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}
		if (p == PopupMenuTrait.POPUP_SAVE) {
			ChangedDataManager.checkSaveAll(false);
		}
	}

	@Override
	protected void updateContent() {
		Logging.debug(this, "setHostPropertiesPage");

		Map<String, Map<String, Object>> depotPropertiesForPermittedDepots = persistenceController
				.getDataServices().depot.getDepotPropertiesForPermittedDepots();

		if (hostUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(hostUpdateCollection);
		}

		String depot = depotSupplier.get();

		hostUpdateCollection = new HostUpdateCollection(depot, depotPropertiesForPermittedDepots.get(depot));
		UpdateCollectionManager.addToGlobalUpdateCollection(hostUpdateCollection);

		Map<String, Object> depotMap = depotPropertiesForPermittedDepots.get(depot);

		Logging.debug(this, "initMultipleHosts ", " configs  ", depotMap);

		editMapPanel.getMapTableModel()
				.setReadOnlyEntries(OpsiServiceNOMPersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT);

		Logging.debug(this, "derive Map ", depotMap);

		deriveDepotMap(depotMap);
		editMapPanel.setEditableMap(depotMap, deriveOptionsMap(depotMap));
		editMapPanel.updateData(hostUpdateCollection, List.of(depotMap));

		editMapPanel.getMapTableModel()
				.setReadOnlyEntries(OpsiServiceNOMPersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT);
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
				depotMap.put(dataEntry.getKey(), List.of(dataEntry.getValue()));
			}
		}

		return depotMap;
	}
}
