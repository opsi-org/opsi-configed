/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicensesStatistics.java
 *
 */

package de.uib.configed.gui.licenses;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.utils.table.gui.PanelGenEditTable;

public class PanelLicensesStatistics extends MultiTablePanel {
	private static final int MIN_VSIZE = 50;

	private PanelGenEditTable panelStatistics;

	public PanelLicensesStatistics(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelStatistics = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleStatistics"), false, 0, null, true);
		panelStatistics.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelStatistics.setFiltering();

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

	public PanelGenEditTable getPanelStatistics() {
		return panelStatistics;
	}

	@Override
	public void reset() {
		if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_STATISTICS)) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			ConfigedMain.getLicensesFrame().setCursor(Globals.WAIT_CURSOR);
			SwingUtilities.invokeLater(() -> {
				panelStatistics.reload();
				ConfigedMain.getLicensesFrame().setCursor(null);
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
