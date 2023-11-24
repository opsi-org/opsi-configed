/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicencesReconciliation.java
 *
 */

package de.uib.configed.gui.licences;

import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelLicencesReconciliation;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelLicencesReconciliation extends MultiTablePanel {
	private PanelGenEditTable panelReconciliation;

	/** Creates new form panelLicencesReconciliation */
	public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController) {
		super(licencesReconciliationController);
		initComponents();
	}

	private void initComponents() {
		panelReconciliation = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), 0, false, 0, true, null,
				true);
		panelReconciliation.setMasterFrame(ConfigedMain.getLicencesFrame());
		panelReconciliation.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelReconciliation.showFilterIcon(true);
		panelReconciliation.setFiltering(true);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(panelReconciliation,
						GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE))
				.addContainerGap());

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panelReconciliation, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));
	}

	public PanelGenEditTable getPanelReconciliation() {
		return panelReconciliation;
	}

	@Override
	public void reset() {
		if (CacheManager.getInstance().getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class) == null) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			ConfigedMain.getLicencesFrame().setCursor(Globals.WAIT_CURSOR);
			SwingUtilities.invokeLater(() -> {
				panelReconciliation.reload();
				ConfigedMain.getLicencesFrame().setCursor(null);
				ConfigedMain.getMainFrame().disactivateLoadingCursor();
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
