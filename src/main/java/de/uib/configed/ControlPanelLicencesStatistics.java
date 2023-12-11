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

import de.uib.configed.gui.licences.PanelLicencesStatistics;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;

public class ControlPanelLicencesStatistics extends AbstractControlMultiTablePanel {
	private PanelLicencesStatistics thePanel;
	private GenTableModel modelStatistics;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	public ControlPanelLicencesStatistics(ConfigedMain configedMain) {
		thePanel = new PanelLicencesStatistics(this);
		this.configedMain = configedMain;

		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		List<String> columnNames;
		List<String> classNames;

		// --- getPanelStatistics()
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
				columnNames);
		modelStatistics = new GenTableModel(updateItemFactoryStatistics,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded() && !configedMain.isInitialLicenseDataLoading()) {
							persistenceController.reloadData(ReloadEvent.STATISTICS_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map retrieveMap() {
						Logging.info(this, "retrieveMap() for modelStatistics");
						if (CacheManager.getInstance().getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION,
								Map.class) == null) {
							return new HashMap<>();
						}
						return !configedMain.isInitialLicenseDataLoading()
								? persistenceController.getSoftwareDataService().getLicenseStatistics()
								: new HashMap<>();
					}
				})), 0, thePanel.getPanelStatistics(), updateCollection);
		updateItemFactoryStatistics.setSource(modelStatistics);

		tableModels.add(modelStatistics);
		tablePanes.add(thePanel.getPanelStatistics());

		modelStatistics.reset();
		thePanel.getPanelStatistics().setTableModel(modelStatistics);
		modelStatistics.setEditableColumns(new int[] {});
		thePanel.getPanelStatistics().setEmphasizedColumns(new int[] {});
	}
}
