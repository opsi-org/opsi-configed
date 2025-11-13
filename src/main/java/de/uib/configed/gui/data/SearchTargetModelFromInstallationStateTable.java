/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.data;

import java.util.Arrays;

import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromTable;
import de.uib.configed.share.logging.Logging;

public class SearchTargetModelFromInstallationStateTable extends SearchTargetModelFromTable {
	private PanelProductSettings panelProductSettings;

	public SearchTargetModelFromInstallationStateTable(PanelProductSettings panelProductSettings) {
		super(panelProductSettings.getProductTable());
		Logging.info(this, "table null? ", table == null);

		this.panelProductSettings = panelProductSettings;
	}

	@Override
	public void setCursorRow(int row) {
		Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
	}

	@Override
	public void setFiltered(boolean filtered) {
		if (filtered) {
			selectedRows = table.getSelectedRows();

			int[] modelRowFilter = new int[selectedRows.length];
			for (int i = 0; i < selectedRows.length; i++) {
				modelRowFilter[i] = table.convertRowIndexToModel(selectedRows[i]);
			}

			Logging.info(this, "setFiltered modelRowFilter ", Arrays.toString(modelRowFilter));

			if (selectedRows.length != 0) {
				panelProductSettings.getProductTable().reduceToSelected();
			}
		} else {
			panelProductSettings.valueChanged(false);
		}
	}
}
