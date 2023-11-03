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
import utils.Utils;

public class PanelLicencesReconciliation extends MultiTablePanel {
	private PanelGenEditTable panelReconciliation;

	private int minVSize = 50;
	private int tablesMaxWidth = 1000;

	private ConfigedMain configedMain;

	/** Creates new form panelLicencesReconciliation */
	public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController,
			ConfigedMain configedMain) {
		super(licencesReconciliationController);
		this.configedMain = configedMain;
		initComponents();
	}

	private void initComponents() {
		panelReconciliation = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), tablesMaxWidth, false, // editing
				0, true, null, true);
		panelReconciliation.setMasterFrame(Utils.getMasterFrame());
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

		layout.setVerticalGroup(layout.createSequentialGroup().addContainerGap()
				.addComponent(panelReconciliation, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));
	}

	public PanelGenEditTable getPanelReconciliation() {
		return panelReconciliation;
	}

	@Override
	public void reset() {
		if (CacheManager.getInstance().getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class) == null) {
			ConfigedMain.getMainFrame()
					.activateLoadingPane(Configed.getResourceValue("PanelLicencesReconciliation.loading.text"));
			ConfigedMain.getMainFrame().activateLoadingCursor();
			configedMain.getLicencesFrame().setCursor(Globals.WAIT_CURSOR);
			SwingUtilities.invokeLater(() -> {
				panelReconciliation.reload();
				configedMain.getLicencesFrame().setCursor(null);
				ConfigedMain.getMainFrame().disactivateLoadingCursor();
				ConfigedMain.getMainFrame().disactivateLoadingPane();
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
