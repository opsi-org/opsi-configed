package de.uib.configed;

import java.util.Map;
import java.util.Vector;

import de.uib.configed.gui.licences.PanelLicencesStatistics;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelLicencesStatistics extends ControlMultiTablePanel {
	PanelLicencesStatistics thePanel;
	GenTableModel modelStatistics;

	PersistenceController persist;

	boolean initialized = false;

	public ControlPanelLicencesStatistics(PersistenceController persist) {
		thePanel = new PanelLicencesStatistics(this);
		this.persist = persist;
		init();
	}

	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	public void init() {
		updateCollection = new TableUpdateCollection();

		Vector<String> columnNames;
		Vector<String> classNames;

		//--- panelStatistics
		columnNames = new Vector<>();
		columnNames.add("licensePoolId");
		columnNames.add("licence_options");
		columnNames.add("used_by_opsi");
		columnNames.add("remaining_opsi");
		columnNames.add("SWinventory_used");
		columnNames.add("SWinventory_remaining");
		classNames = new Vector<>();
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
					public Map retrieveMap() {
						logging.info(this, "retrieveMap() for modelStatistics");
						if (initialized)
							persist.reconciliationInfoRequestRefresh();
						else
							initialized = true;
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
