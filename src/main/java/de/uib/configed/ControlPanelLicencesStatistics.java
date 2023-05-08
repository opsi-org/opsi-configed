package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.gui.licences.PanelLicencesStatistics;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelLicencesStatistics extends AbstractControlMultiTablePanel {
	PanelLicencesStatistics thePanel;
	GenTableModel modelStatistics;

	private AbstractPersistenceController persist;

	boolean initialized;

	public ControlPanelLicencesStatistics(AbstractPersistenceController persist) {
		thePanel = new PanelLicencesStatistics(this);
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

		// --- panelStatistics
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("licence_options");
		columnNames.add("used_by_opsi");
		columnNames.add("remaining_opsi");
		columnNames.add("SWinventory_used");
		columnNames.add("SWinventory_remaining");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryStatistics = new MapTableUpdateItemFactory(modelStatistics,
				columnNames, classNames, 0);
		modelStatistics = new GenTableModel(updateItemFactoryStatistics,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map retrieveMap() {
						Logging.info(this, "retrieveMap() for modelStatistics");
						if (initialized) {
							persist.reconciliationInfoRequestRefresh();
						} else {
							initialized = true;
						}
						return persist.getLicenceStatistics();
					}
				})), 0, thePanel.panelStatistics, updateCollection);
		updateItemFactoryStatistics.setSource(modelStatistics);

		tableModels.add(modelStatistics);
		tablePanes.add(thePanel.panelStatistics);

		modelStatistics.reset();
		thePanel.panelStatistics.setTableModel(modelStatistics);
		modelStatistics.setEditableColumns(new int[] {});
		thePanel.panelStatistics.setEmphasizedColumns(new int[] {});

	}
}
