package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.gui.licences.PanelLicencesReconciliation;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelLicencesReconciliation extends AbstractControlMultiTablePanel {

	PanelLicencesReconciliation thePanel;
	GenTableModel modelLicencesReconciliation;

	AbstractPersistenceController persist;

	boolean initialized = false;

	public ControlPanelLicencesReconciliation(AbstractPersistenceController persist) {
		thePanel = new PanelLicencesReconciliation(this);
		this.persist = persist;

		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new TableUpdateCollection();

		List<String> columnNames;
		List<String> classNames;

		List<String> extraHostFields = persist.getServerConfigStrings(
				AbstractPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION);

		// --- panelLicencesReconciliation
		columnNames = new ArrayList<>();
		classNames = new ArrayList<>();

		columnNames.add("hostId");

		for (String fieldName : extraHostFields) {
			columnNames.add(fieldName);
			classNames.add("java.lang.String");
		}

		columnNames.add("licensePoolId");
		columnNames.add("used_by_opsi");
		final int index_used_by_opsi = columnNames.size() - 1;
		columnNames.add("SWinventory_used");
		final int index_SWinventory_used = columnNames.size() - 1;
		Logging.debug(this, "columnNames: " + columnNames);
		Logging.debug(this, "cols index_used_by_opsi  " + index_used_by_opsi + " , " + index_SWinventory_used);

		classNames.add("java.lang.String");

		classNames.add("java.lang.String");
		classNames.add("java.lang.Boolean");
		classNames.add("java.lang.Boolean");
		MapTableUpdateItemFactory updateItemFactoryLicencesReconciliation = new MapTableUpdateItemFactory(
				modelLicencesReconciliation, columnNames, classNames, 0);
		modelLicencesReconciliation = new GenTableModel(updateItemFactoryLicencesReconciliation,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map retrieveMap() {
						Logging.debug(this, "retrieveMap");
						if (initialized) {
							persist.reconciliationInfoRequestRefresh();
						}
						initialized = true;
						return persist.getLicencesReconciliation();
					}
				})),

				-1, new int[] { 0, 1 },

				thePanel.panelReconciliation, updateCollection);

		// filter which guarantees that clients are only shown when they have entries
		modelLicencesReconciliation.setFilterCondition(new TableModelFilterCondition() {
			@Override
			public void setFilter(Set<Object> filterParam) {
				// Implementing TableModelFilterCondition
			}

			@Override
			public boolean test(List<Object> row) {
				return ((Boolean) row.get(index_used_by_opsi)) || ((Boolean) row.get(index_SWinventory_used));
			}
		});

		updateItemFactoryLicencesReconciliation.setSource(modelLicencesReconciliation);

		tableModels.add(modelLicencesReconciliation);
		tablePanes.add(thePanel.panelReconciliation);

		modelLicencesReconciliation.reset();
		thePanel.panelReconciliation.setTableModel(modelLicencesReconciliation);
		modelLicencesReconciliation.setEditableColumns(new int[] {});
		thePanel.panelReconciliation.setEmphasizedColumns(new int[] {});

		// --- PopupMenu

		// special treatment of columns
		javax.swing.table.TableColumn col;

		col = thePanel.panelReconciliation.getColumnModel().getColumn(index_used_by_opsi);
		col.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);

		col = thePanel.panelReconciliation.getColumnModel().getColumn(index_SWinventory_used);
		col.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer());
		col.setPreferredWidth(130);
		col.setMaxWidth(200);

		// updates
		thePanel.panelReconciliation.setUpdateController(new MapItemsUpdateController(thePanel.panelReconciliation,
				modelLicencesReconciliation, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editLicencesReconciliation((String) rowmap.get("hostId"),
								(String) rowmap.get("licensePoolId"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencesReconciliation.requestReload();
						return persist.deleteLicencesReconciliation((String) rowmap.get("hostId"),
								(String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		Integer[] searchCols = new Integer[2];
		searchCols[0] = 0;
		searchCols[1] = 1;

		thePanel.panelReconciliation.setSearchColumns(searchCols);
		thePanel.panelReconciliation.setSearchSelectMode(true);

	}
}
