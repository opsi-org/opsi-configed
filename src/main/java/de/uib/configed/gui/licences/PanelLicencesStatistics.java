/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicencesStatistics.java
 *
 */

package de.uib.configed.gui.licences;

import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelLicencesStatistics extends MultiTablePanel {
	private static final int MIN_VSIZE = 50;

	private PanelGenEditTable panelStatistics;

	/** Creates new form panelLicencesStatistics */
	public PanelLicencesStatistics(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelStatistics = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleStatistics"), false, 0, true, null, true);
		panelStatistics.setMasterFrame(ConfigedMain.getLicencesFrame());
		panelStatistics.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelStatistics.showFilterIcon(true);
		panelStatistics.setFiltering(true);

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
		if (CacheManager.getInstance().getCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS, Map.class) == null) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			ConfigedMain.getLicencesFrame().setCursor(Globals.WAIT_CURSOR);
			SwingUtilities.invokeLater(() -> {
				panelStatistics.reload();
				ConfigedMain.getLicencesFrame().setCursor(null);
				ConfigedMain.getMainFrame().disactivateLoadingCursor();
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
