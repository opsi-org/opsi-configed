/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Arrays;

import javax.swing.JTable;

import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModelFromTable;

public class SearchTargetModelFromInstallationStateTable extends SearchTargetModelFromTable {
	private PanelProductSettings panelProductSettings;

	public SearchTargetModelFromInstallationStateTable(JTable table, PanelProductSettings panelProductSettings) {
		super(table);
		Logging.info(this.getClass(), "table null? " + (table == null));

		this.panelProductSettings = panelProductSettings;
	}

	@Override
	public void setCursorRow(int row) {
		Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
	}

	@Override
	public void setFiltered(boolean b) {
		if (!filtered) {
			selectedRows = table.getSelectedRows();
		}

		if (b && selectedRows.length > 0) {
			int[] modelRowFilter = new int[selectedRows.length];
			for (int i = 0; i < selectedRows.length; i++) {
				modelRowFilter[i] = table.convertRowIndexToModel(selectedRows[i]);
			}

			Logging.info(this, "setFiltered modelRowFilter " + Arrays.toString(modelRowFilter));

			panelProductSettings.reduceToSelected();
		} else {
			panelProductSettings.showAll();
		}
		filtered = b;
	}
}
