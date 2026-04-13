/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

/*
 * PanelLicensesStatistics.java
 *
 */

package de.uib.configed.gui.features.licenses;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import net.miginfocom.swing.MigLayout;

public class PanelLicensesStatistics extends MultiTablePanel {
	private static final int MIN_VSIZE = 50;

	private PanelGenEdit panelStatistics;

	public PanelLicensesStatistics(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelStatistics = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleStatistics"),
				false, 0, new int[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF }, true);
		panelStatistics.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelStatistics.setFilterKey(FilterKey.LICENSES_STATISTICS_TABLE);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelStatistics.getTableSearchPane().setFiltering();

		this.setLayout(new MigLayout("insets 0, fill", "", "[]0"));
		this.add(panelStatistics, "grow, hmin " + MIN_VSIZE + ", gapbottom " + Globals.MIN_GAP_SIZE);
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
