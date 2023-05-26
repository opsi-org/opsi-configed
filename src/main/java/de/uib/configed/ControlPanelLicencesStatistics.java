/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.gui.licences.PanelLicencesStatistics;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;

public class ControlPanelLicencesStatistics extends AbstractControlMultiTablePanel {
	private PanelLicencesStatistics thePanel;
	private GenTableModel modelStatistics;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean initialized;

	public ControlPanelLicencesStatistics() {
		thePanel = new PanelLicencesStatistics(this);

		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

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
							persistenceController.reconciliationInfoRequestRefresh();
						} else {
							initialized = true;
						}
						return persistenceController.getLicenceStatistics();
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
