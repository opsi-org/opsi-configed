/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.features.licenses.MultiTablePanel;
import de.uib.configed.gui.features.licenses.PanelLicensesStatistics;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.MapRetriever;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.share.logging.Logging;

public class ControlPanelLicensesStatistics extends AbstractControlMultiTablePanel {
	private PanelLicensesStatistics thePanel;
	private GenTableModel modelStatistics;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ControlPanelLicensesStatistics() {
		thePanel = new PanelLicensesStatistics(this);

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
						persistenceController.reloadData(ReloadEvent.STATISTICS_DATA_RELOAD.toString());
					}

					@Override
					public Map retrieveMap() {
						Logging.info(this, "retrieveMap() for modelStatistics");
						if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
							return new HashMap<>();
						}

						return persistenceController.getDataServices().software.getLicenseStatistics();
					}
				})), 0, thePanel.getPanelStatistics(), updateCollection);
		updateItemFactoryStatistics.setSource(modelStatistics);

		tableModels.add(modelStatistics);
		panelGenEdits.add(thePanel.getPanelStatistics());

		modelStatistics.reset();
		thePanel.getPanelStatistics().setTableModel(modelStatistics);

		thePanel.getPanelStatistics().getGenEditTable()
				.setRowSorter(new StringIntegerRowSorter(modelStatistics, Set.of(1, 2, 3, 4, 5)));
		thePanel.getPanelStatistics().restoreFilter();
		modelStatistics.setEditableColumns(new int[] {});
	}
}
