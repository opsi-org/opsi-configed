/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicensesReconciliation.java
 *
 */

package de.uib.configed.gui.features.licenses;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ControlPanelLicensesReconciliation;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import net.miginfocom.swing.MigLayout;

public class PanelLicensesReconciliation extends MultiTablePanel {
	private PanelGenEdit panelReconciliation;

	public PanelLicensesReconciliation(ControlPanelLicensesReconciliation licensesReconciliationController) {
		super(licensesReconciliationController);
		initComponents();
	}

	private void initComponents() {
		panelReconciliation = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleReconciliation"), false, 0, null, true);
		panelReconciliation.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelReconciliation.setFilterKey(FilterKey.LICENSE_RECONCILIATION_TABLE);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelReconciliation.getTableSearchPane().setFiltering();

		this.setLayout(new MigLayout("insets 0, fill", "", "[]0"));
		this.add(panelReconciliation, "grow, hmin 50, gapbottom " + Globals.MIN_GAP_SIZE);

	}

	public PanelGenEdit getPanelReconciliation() {
		return panelReconciliation;
	}

	@Override
	public void reset() {
		if (!CacheManager.getInstance().isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			SwingUtilities.invokeLater(() -> {
				panelReconciliation.reload();
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			});
		} else if (panelReconciliation.getTableModel().getRows().isEmpty()) {
			panelReconciliation.getTableModel().resetLocally();
			controller.refreshPanelGenEdits();
			controller.initializeVisualSettings();
		} else {
			super.reset();
		}
	}

	@Override
	public void load() {
		reset();
	}
}
