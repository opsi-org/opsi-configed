/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicensesReconciliation.java
 *
 */

package de.uib.configed.gui.licenses;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelLicensesReconciliation;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.utils.table.gui.PanelGenEditTable;

public class PanelLicensesReconciliation extends MultiTablePanel {
	private PanelGenEditTable panelReconciliation;

	public PanelLicensesReconciliation(ControlPanelLicensesReconciliation licensesReconciliationController) {
		super(licensesReconciliationController);
		initComponents();
	}

	private void initComponents() {
		panelReconciliation = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleReconciliation"), false, 0, null, true);
		panelReconciliation.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelReconciliation.setFiltering(true);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(panelReconciliation,
						GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE))
				.addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panelReconciliation, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));
	}

	public PanelGenEditTable getPanelReconciliation() {
		return panelReconciliation;
	}

	@Override
	public void reset() {
		if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			ConfigedMain.getLicensesFrame().setCursor(Globals.WAIT_CURSOR);
			SwingUtilities.invokeLater(() -> {
				panelReconciliation.reload();
				ConfigedMain.getLicensesFrame().setCursor(null);
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			});
		} else if (panelReconciliation.getTableModel().getRows().isEmpty()) {
			panelReconciliation.getTableModel().resetLocally();
			controller.refreshTables();
			controller.initializeVisualSettings();
		} else {
			super.reset();
		}
	}
}
