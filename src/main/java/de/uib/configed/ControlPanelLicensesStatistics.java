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

import de.uib.configed.gui.licenses.MultiTablePanel;
import de.uib.configed.gui.licenses.PanelLicensesStatistics;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapRetriever;
import de.uib.utils.table.provider.RetrieverMapSource;
import de.uib.utils.table.updates.MapBasedTableEditItem;
import de.uib.utils.table.updates.MapTableUpdateItemFactory;

public class ControlPanelLicensesStatistics extends AbstractControlMultiTablePanel {
	private PanelLicensesStatistics thePanel;
	private GenTableModel modelStatistics;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	public ControlPanelLicensesStatistics(ConfigedMain configedMain) {
		thePanel = new PanelLicensesStatistics(this);
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

		List<String> columnNames;

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("license_options");
		columnNames.add("used_by_opsi");
		columnNames.add("remaining_opsi");
		columnNames.add("SWinventory_used");
		columnNames.add("SWinventory_remaining");
		MapTableUpdateItemFactory updateItemFactoryStatistics = new MapTableUpdateItemFactory(modelStatistics,
				columnNames);
		modelStatistics = new GenTableModel(updateItemFactoryStatistics,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded() && !configedMain.isInitialLicenseDataLoading()) {
							persistenceController.reloadData(ReloadEvent.STATISTICS_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map retrieveMap() {
						Logging.info(this, "retrieveMap() for modelStatistics");
						if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
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
	}
}
