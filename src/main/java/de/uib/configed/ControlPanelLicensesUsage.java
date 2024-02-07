/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licenses.PanelLicensesUsage;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;

public class ControlPanelLicensesUsage extends AbstractControlMultiTablePanel {
	private PanelLicensesUsage thePanel;

	private GenTableModel modelLicensesUsage;
	private GenTableModel modelLicensepools;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public ControlPanelLicensesUsage(ConfigedMain configedMain) {
		thePanel = new PanelLicensesUsage(this);
		this.configedMain = configedMain;

		init();
	}

	public String getSoftwareLicenseReservation(String clientId) {
		if (clientId == null || clientId.isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectClient"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		List<String> selectedLPoolIds = thePanel.getPanelLicensePools().getSelectedKeys();

		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
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
	public TabClientAdapter getTabClient() {
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
		choicesAllHosts.set(0, "");
		Collections.sort(choicesAllHosts);
		Logging.debug(this, "choicesAllHosts " + choicesAllHosts);
		DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
		thePanel.setClientsSource(comboBoxModel);
	}

	private void initPanels() {
		List<String> columnNames;

		// --- panelLicensesUsage
		columnNames = new ArrayList<>();
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
						if (!configedMain.isAllLicenseDataReloaded()) {
							persistenceController
									.reloadData(ReloadEvent.SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getRowsLicensesUsagePD();
					}
				})), -1, new int[] { 0, 1, 2 }, thePanel.getPanelUsage(), updateCollection, true);
		updateItemFactoryLicensesUsage.setSource(modelLicensesUsage);

		tableModels.add(modelLicensesUsage);
		tablePanes.add(thePanel.getPanelUsage());

		modelLicensesUsage.reset();
		thePanel.getPanelUsage().setTableModel(modelLicensesUsage);
		modelLicensesUsage.setEditableColumns(new int[] { 3, 4 });

		TableColumn col;
		col = thePanel.getPanelUsage().getColumnModel().getColumn(4);
		col.setCellEditor(new CellEditor4TableText());

		setPanelUsageUpdateController();

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicensepools = new MapTableUpdateItemFactory(modelLicensepools,
				columnNames);
		modelLicensepools = new GenTableModel(updateItemFactoryLicensepools, configedMain.licensePoolTableProvider, 0,
				thePanel.getPanelLicensePools(), updateCollection);
		updateItemFactoryLicensepools.setSource(modelLicensepools);

		tableModels.add(modelLicensepools);
		tablePanes.add(thePanel.getPanelLicensePools());

		modelLicensepools.reset();
		thePanel.getPanelLicensePools().setTableModel(modelLicensepools);
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
