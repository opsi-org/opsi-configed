/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableColumn;

import de.uib.configed.gui.licences.PanelLicencesReconciliation;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.CheckBoxTableCellRenderer;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;

public class ControlPanelLicencesReconciliation extends AbstractControlMultiTablePanel {

	private PanelLicencesReconciliation thePanel;
	private GenTableModel modelLicencesReconciliation;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean initialized;

	private int indexUsedByOpsi;
	private int indexSWInventoryUsed;

	public ControlPanelLicencesReconciliation() {
		thePanel = new PanelLicencesReconciliation(this);

		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

		initPanels();

		// special treatment of columns
		initTreatmentOfColumns();

		// updates
		thePanel.getPanelReconciliation().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelReconciliation(), modelLicencesReconciliation, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return "";
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencesReconciliation.requestReload();
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
		List<String> classNames;

		List<String> extraHostFields = persistenceController.getConfigDataService().getServerConfigStrings(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION);

		// --- panelLicencesReconciliation
		columnNames = new ArrayList<>();
		classNames = new ArrayList<>();

		columnNames.add(OpsiServiceNOMPersistenceController.HOST_KEY);

		for (String fieldName : extraHostFields) {
			columnNames.add(fieldName);
			classNames.add("java.lang.String");
		}

		columnNames.add("licensePoolId");
		columnNames.add("used_by_opsi");
		indexUsedByOpsi = columnNames.size() - 1;
		columnNames.add("SWinventory_used");
		indexSWInventoryUsed = columnNames.size() - 1;
		Logging.debug(this, "columnNames: " + columnNames);
		Logging.debug(this, "cols index_used_by_opsi  " + indexUsedByOpsi + " , " + indexSWInventoryUsed);

		classNames.add("java.lang.String");

		classNames.add("java.lang.String");
		classNames.add("java.lang.Boolean");
		classNames.add("java.lang.Boolean");
		MapTableUpdateItemFactory updateItemFactoryLicencesReconciliation = new MapTableUpdateItemFactory(
				modelLicencesReconciliation, columnNames, 0);
		modelLicencesReconciliation = new GenTableModel(updateItemFactoryLicencesReconciliation,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						Logging.debug(this, "retrieveMap");
						if (initialized) {
							persistenceController.reloadData(ReloadEvent.RECONCILIATION_INFO_RELOAD.toString());
						}
						initialized = true;
						return persistenceController.getSoftwareDataService().getLicensesReconciliationPD();
					}
				})),

				-1, new int[] { 0, 1 },

				thePanel.getPanelReconciliation(), updateCollection);

		// filter which guarantees that clients are only shown when they have entries
		modelLicencesReconciliation.setFilterCondition(new TableModelFilterCondition() {
			@Override
			public void setFilter(Set<Object> filterParam) {
				// Implementing TableModelFilterCondition
			}

			@Override
			public boolean test(List<Object> row) {
				return ((Boolean) row.get(indexUsedByOpsi)) || ((Boolean) row.get(indexSWInventoryUsed));
			}
		});

		updateItemFactoryLicencesReconciliation.setSource(modelLicencesReconciliation);

		tableModels.add(modelLicencesReconciliation);
		tablePanes.add(thePanel.getPanelReconciliation());

		modelLicencesReconciliation.reset();
		thePanel.getPanelReconciliation().setTableModel(modelLicencesReconciliation);
		modelLicencesReconciliation.setEditableColumns(new int[] {});
		thePanel.getPanelReconciliation().setEmphasizedColumns(new int[] {});
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
