/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import de.uib.configed.gui.licenses.MultiTablePanel;
import de.uib.utils.Utils;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.updates.MapBasedTableEditItem;

public abstract class AbstractControlMultiTablePanel {
	protected List<GenTableModel> tableModels = new ArrayList<>();

	protected List<PanelGenEditTable> tablePanes = new ArrayList<>();

	protected List<MapBasedTableEditItem> updateCollection = new ArrayList<>();

	public abstract MultiTablePanel getTabClient();

	public List<GenTableModel> getTableModels() {
		return tableModels;
	}

	public List<PanelGenEditTable> getTablePanes() {
		return tablePanes;
	}

	public abstract void init();

	/**
	 * called by the MultiTablePanel reset method overwrite for the real content
	 */
	public void initializeVisualSettings() {
	}

	public void refreshTables() {
		for (GenTableModel tableModel : tableModels) {
			tableModel.invalidate();
			tableModel.reset();
		}

		for (PanelGenEditTable tablePanel : tablePanes) {
			tablePanel.setDataChanged(false);
		}
	}

	public boolean mayLeave() {
		boolean change = false;

		Iterator<PanelGenEditTable> iterP = tablePanes.iterator();

		while (!change && iterP.hasNext()) {
			PanelGenEditTable p = iterP.next();
			change = p.isDataChanged();
		}

		if (change) {
			int returnedOption = JOptionPane.showConfirmDialog(Utils.getMasterFrame(),
					Configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.text"),
					Configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (returnedOption == JOptionPane.YES_OPTION) {
				return true;
			}

			Utils.getMasterFrame().setVisible(true);
		} else {
			return true;
		}

		return false;
	}
}
