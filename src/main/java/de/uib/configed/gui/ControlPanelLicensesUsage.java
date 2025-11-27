/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.features.licenses.LicenseManagement;
import de.uib.configed.gui.features.licenses.MultiTablePanel;
import de.uib.configed.gui.features.licenses.PanelLicensesUsage;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.CellInputDialogEditor;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.MapRetriever;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapBasedUpdater;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.gui.type.licenses.LicenseUsageEntry;
import de.uib.configed.share.logging.Logging;

public class ControlPanelLicensesUsage extends AbstractControlMultiTablePanel {
	private PanelLicensesUsage thePanel;

	private GenTableModel modelLicensesUsage;
	private GenTableModel modelLicensepools;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private LicenseManagement licensesPane;

	public ControlPanelLicensesUsage(ConfigedMain configedMain, LicenseManagement licensesPane) {
		thePanel = new PanelLicensesUsage(this);
		this.configedMain = configedMain;
		this.licensesPane = licensesPane;

		init();
	}

	public String getSoftwareLicenseReservation(String clientId) {
		if (clientId == null || clientId.isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectClient"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		List<String> selectedLPoolIds = thePanel.getPanelLicensePools().getSelectedKeys();

		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectOneLicensepool"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		String licensePoolId = selectedLPoolIds.iterator().next();
		String result = persistenceController.getLicenseDataService().getLicenseUsage(clientId, licensePoolId);

		if (result != null) {
			thePanel.getPanelUsage().reload();

			thePanel.getPanelUsage().moveToKeyValue(result);
		} else {
			thePanel.getPanelUsage().moveToValue(clientId, 0, true);
		}

		return result;
	}

	@Override
	public MultiTablePanel getTabClient() {
		return thePanel;
	}

	@Override
	public void initializeVisualSettings() {
		thePanel.setDivider();
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		initPanels();

		List<String> choicesAllHosts = new ArrayList<>(persistenceController.getHostInfoCollections()
				.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients()));
		if (!choicesAllHosts.isEmpty()) {
			choicesAllHosts.set(0, "");
			Collections.sort(choicesAllHosts);
		}
		Logging.debug(this, "choicesAllHosts ", choicesAllHosts);
		DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
		thePanel.setClientsSource(comboBoxModel);
	}

	private void initPanels() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add(LicenseUsageEntry.CLIENT_ID_KEY);
		columnNames.add(LicenseUsageEntry.LICENSE_ID_KEY);
		columnNames.add(LicenseUsageEntry.LICENSE_POOL_ID_KEY);
		columnNames.add(LicenseUsageEntry.LICENSE_KEY_KEY);
		columnNames.add(LicenseUsageEntry.NOTES_KEY);
		MapTableUpdateItemFactory updateItemFactoryLicensesUsage = new MapTableUpdateItemFactory(modelLicensesUsage,
				columnNames);
		modelLicensesUsage = new GenTableModel(updateItemFactoryLicensesUsage,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						persistenceController.reloadData(ReloadEvent.LICENSE_ON_CLIENT_DATA_RELOAD.toString());
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getRowsLicensesUsagePD();
					}
				})), -1, new int[] { 0, 1, 2 }, thePanel.getPanelUsage(), updateCollection, true);
		updateItemFactoryLicensesUsage.setSource(modelLicensesUsage);

		tableModels.add(modelLicensesUsage);
		panelGenEdits.add(thePanel.getPanelUsage());

		modelLicensesUsage.reset();
		thePanel.getPanelUsage().setTableModel(modelLicensesUsage);
		thePanel.getPanelUsage().restoreFilter();
		modelLicensesUsage.setEditableColumns(new int[] { 3, 4 });

		TableColumn col = thePanel.getPanelUsage().getGenEditTable().getColumnModel().getColumn(4);
		col.setCellEditor(new CellInputDialogEditor());

		setPanelUsageUpdateController();

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicensepools = new MapTableUpdateItemFactory(modelLicensepools,
				columnNames);
		modelLicensepools = new GenTableModel(updateItemFactoryLicensepools, licensesPane.getLicensePoolTableProvider(),
				0, thePanel.getPanelLicensePools(), updateCollection);
		updateItemFactoryLicensepools.setSource(modelLicensepools);

		tableModels.add(modelLicensepools);
		panelGenEdits.add(thePanel.getPanelLicensePools());

		modelLicensepools.reset();
		thePanel.getPanelLicensePools().setTableModel(modelLicensepools);
		thePanel.getPanelLicensePools().restoreFilter();
	}

	private void setPanelUsageUpdateController() {
		thePanel.getPanelUsage().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelUsage(), modelLicensesUsage, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getLicenseDataService().editLicenseUsage(
								(String) rowmap.get(LicenseUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenseUsageEntry.LICENSE_ID_KEY),
								(String) rowmap.get(LicenseUsageEntry.LICENSE_POOL_ID_KEY),
								(String) rowmap.get(LicenseUsageEntry.LICENSE_KEY_KEY),
								(String) rowmap.get(LicenseUsageEntry.NOTES_KEY));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensesUsage.requestReload();
						return persistenceController.getLicenseDataService().deleteLicenseUsage(
								(String) rowmap.get(LicenseUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenseUsageEntry.LICENSE_ID_KEY),
								(String) rowmap.get(LicenseUsageEntry.LICENSE_POOL_ID_KEY));
					}
				}, updateCollection));
	}
}
