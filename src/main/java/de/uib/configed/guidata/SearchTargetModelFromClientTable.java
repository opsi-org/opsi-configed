/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Arrays;

import javax.swing.JTable;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModelFromTable;

public class SearchTargetModelFromClientTable extends SearchTargetModelFromTable {

	public SearchTargetModelFromClientTable(JTable table) {
		super(table);
		Logging.info(this.getClass(), "table null? " + (table == null));
	}

	@Override
	public void setCursorRow(int row) {
		Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
	}

	@Override
	public void setFiltered(boolean b) {
		if (!filtered) {
			viewRowfilter = table.getSelectedRows();
		}

		if (b && viewRowfilter.length > 0) {
			int[] modelRowFilter = new int[viewRowfilter.length];
			for (int i = 0; i < viewRowfilter.length; i++) {
				modelRowFilter[i] = table.convertRowIndexToModel(viewRowfilter[i]);
			}

			Logging.info(this, "setFiltered modelRowFilter " + Arrays.toString(modelRowFilter));

			// TODO, filter model, copy from button on top
			//panelProductSettings.reduceToSelected();
		} else {
			//panelProductSettings.showAll();
		}
		filtered = b;
	}
}
