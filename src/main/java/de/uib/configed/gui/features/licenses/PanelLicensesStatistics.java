/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicensesStatistics.java
 *
 */

package de.uib.configed.gui.features.licenses;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;

public class PanelLicensesStatistics extends MultiTablePanel {
	private static final int MIN_VSIZE = 50;

	private PanelGenEdit panelStatistics;

	public PanelLicensesStatistics(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelStatistics = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleStatistics"),
				false, 0, null, true);
		panelStatistics.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelStatistics.setFilterKey(FilterKey.LICENSES_STATISTICS_TABLE);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelStatistics.getTableSearchPane().setFiltering();

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(panelStatistics,
						GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE))
				.addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panelStatistics, MIN_VSIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));
	}

	public PanelGenEdit getPanelStatistics() {
		return panelStatistics;
	}

	@Override
	public void reset() {
		if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_STATISTICS)) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			SwingUtilities.invokeLater(() -> {
				panelStatistics.reload();
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			});
		} else if (panelStatistics.getTableModel().getRows().isEmpty()) {
			panelStatistics.getTableModel().resetLocally();
			controller.refreshTables();
			controller.initializeVisualSettings();
		} else {
			super.reset();
		}
	}
}
