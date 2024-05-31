/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import javax.swing.JTable;

import de.uib.configed.ConfigedMain;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.SearchTargetModelFromTable;

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
		ConfigedMain.getMainFrame().toggleClientFilterAction();
	}
}
