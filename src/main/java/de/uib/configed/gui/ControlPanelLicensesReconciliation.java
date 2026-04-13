/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableColumn;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.features.licenses.MultiTablePanel;
import de.uib.configed.gui.features.licenses.PanelLicensesReconciliation;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.TableModelFilterCondition;
import de.uib.configed.gui.share.table.gui.CheckBoxTableCellRenderer;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapBasedUpdater;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.share.logging.Logging;

public class ControlPanelLicensesReconciliation extends AbstractControlMultiTablePanel {
	private PanelLicensesReconciliation thePanel;
	private GenTableModel modelLicensesReconciliation;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private int indexUsedByOpsi;
	private int indexSWInventoryUsed;

	public ControlPanelLicensesReconciliation() {
		thePanel = new PanelLicensesReconciliation(this);

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
	}

	private void initPanels() {
		List<String> extraHostFields = persistenceController.getDataServices().config.getServerConfigStrings(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION);

		List<String> columnNames = new ArrayList<>();

		columnNames.add(OpsiServiceNOMPersistenceController.HOST_KEY);

		columnNames.addAll(extraHostFields);

		columnNames.add("licensePoolId");
		columnNames.add("used_by_opsi");
		indexUsedByOpsi = columnNames.size() - 1;
		columnNames.add("SWinventory_used");
		indexSWInventoryUsed = columnNames.size() - 1;
		Logging.debug(this, "columnNames: ", columnNames);
		Logging.debug(this, "cols index_used_by_opsi  ", indexUsedByOpsi, " , ", indexSWInventoryUsed);

		MapTableUpdateItemFactory updateItemFactoryLicensesReconciliation = new MapTableUpdateItemFactory(
				modelLicensesReconciliation, columnNames);
		modelLicensesReconciliation = new GenTableModel(updateItemFactoryLicensesReconciliation,
				DefaultTableProvider.createWithRetrieverMapSource(columnNames, ReloadEvent.STATISTICS_DATA_RELOAD,
						() -> CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)
								? persistenceController.getDataServices().software.getLicensesReconciliationPD()
								: new HashMap<>()),
				-1, new int[] { 0, 1 }, thePanel.getPanelReconciliation(), updateCollection);

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
		panelGenEdits.add(thePanel.getPanelReconciliation());

		modelLicensesReconciliation.reset();
		thePanel.getPanelReconciliation().setTableModel(modelLicensesReconciliation);
		thePanel.getPanelReconciliation().restoreFilter();
		modelLicensesReconciliation.setEditableColumns(new int[] {});
	}

	private void initTreatmentOfColumns() {
		TableColumn col;

		col = thePanel.getPanelReconciliation().getGenEditTable().getColumnModel().getColumn(indexUsedByOpsi);
		col.setCellRenderer(new CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);

		col = thePanel.getPanelReconciliation().getGenEditTable().getColumnModel().getColumn(indexSWInventoryUsed);
		col.setCellRenderer(new CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);
	}
}
