/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableColumn;

import de.uib.configed.gui.licenses.MultiTablePanel;
import de.uib.configed.gui.licenses.PanelLicensesReconciliation;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.CheckBoxTableCellRenderer;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;

public class ControlPanelLicensesReconciliation extends AbstractControlMultiTablePanel {
	private PanelLicensesReconciliation thePanel;
	private GenTableModel modelLicensesReconciliation;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private int indexUsedByOpsi;
	private int indexSWInventoryUsed;

	private ConfigedMain configedMain;

	public ControlPanelLicensesReconciliation(ConfigedMain configedMain) {
		thePanel = new PanelLicensesReconciliation(this);
		this.configedMain = configedMain;

		init();
	}

	@Override
	public MultiTablePanel getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		initPanels();

		initTreatmentOfColumns();

		thePanel.getPanelReconciliation().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelReconciliation(), modelLicensesReconciliation, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return "";
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensesReconciliation.requestReload();
						return false;
					}
				}, updateCollection));

		Integer[] searchCols = new Integer[2];
		searchCols[0] = 0;
		searchCols[1] = 1;

		thePanel.getPanelReconciliation().setSearchColumns(searchCols);
		thePanel.getPanelReconciliation().setSearchSelectMode(true);
	}

	private void initPanels() {
		List<String> columnNames;

		List<String> extraHostFields = persistenceController.getConfigDataService().getServerConfigStrings(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION);

		columnNames = new ArrayList<>();

		columnNames.add(OpsiServiceNOMPersistenceController.HOST_KEY);

		for (String fieldName : extraHostFields) {
			columnNames.add(fieldName);
		}

		columnNames.add("licensePoolId");
		columnNames.add("used_by_opsi");
		indexUsedByOpsi = columnNames.size() - 1;
		columnNames.add("SWinventory_used");
		indexSWInventoryUsed = columnNames.size() - 1;
		Logging.debug(this, "columnNames: " + columnNames);
		Logging.debug(this, "cols index_used_by_opsi  " + indexUsedByOpsi + " , " + indexSWInventoryUsed);

		MapTableUpdateItemFactory updateItemFactoryLicensesReconciliation = new MapTableUpdateItemFactory(
				modelLicensesReconciliation, columnNames);
		modelLicensesReconciliation = new GenTableModel(updateItemFactoryLicensesReconciliation,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded() && !configedMain.isInitialLicenseDataLoading()) {
							persistenceController.reloadData(ReloadEvent.STATISTICS_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						Logging.debug(this, "retrieveMap");
						if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
							return new HashMap<>();
						}
						return !configedMain.isInitialLicenseDataLoading()
								? persistenceController.getSoftwareDataService().getLicensesReconciliationPD()
								: new HashMap<>();
					}
				})), -1, new int[] { 0, 1 }, thePanel.getPanelReconciliation(), updateCollection);

		// filter which guarantees that clients are only shown when they have entries
		modelLicensesReconciliation.setFilterCondition(new TableModelFilterCondition() {
			@Override
			public void setFilter(Set<Object> filterParam) {
				// Implementing TableModelFilterCondition
			}

			@Override
			public boolean test(List<Object> row) {
				return ((Boolean) row.get(indexUsedByOpsi)) || ((Boolean) row.get(indexSWInventoryUsed));
			}
		});

		updateItemFactoryLicensesReconciliation.setSource(modelLicensesReconciliation);

		tableModels.add(modelLicensesReconciliation);
		tablePanes.add(thePanel.getPanelReconciliation());

		modelLicensesReconciliation.reset();
		thePanel.getPanelReconciliation().setTableModel(modelLicensesReconciliation);
		modelLicensesReconciliation.setEditableColumns(new int[] {});
	}

	private void initTreatmentOfColumns() {
		TableColumn col;

		col = thePanel.getPanelReconciliation().getColumnModel().getColumn(indexUsedByOpsi);
		col.setCellRenderer(new CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);

		col = thePanel.getPanelReconciliation().getColumnModel().getColumn(indexSWInventoryUsed);
		col.setCellRenderer(new CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);
	}
}
