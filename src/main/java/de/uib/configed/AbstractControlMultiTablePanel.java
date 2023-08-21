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

import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.updates.TableEditItem;
import utils.Utils;

public abstract class AbstractControlMultiTablePanel {
	protected List<GenTableModel> tableModels = new ArrayList<>();

	protected List<PanelGenEditTable> tablePanes = new ArrayList<>();

	protected ArrayList<TableEditItem> updateCollection = new ArrayList<>();

	public abstract TabClientAdapter getTabClient();

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
		Iterator<GenTableModel> iterM = tableModels.iterator();

		while (iterM.hasNext()) {
			GenTableModel m = iterM.next();

			m.invalidate();
			m.reset();
		}

		Iterator<PanelGenEditTable> iterP = tablePanes.iterator();

		while (iterP.hasNext()) {
			PanelGenEditTable p = iterP.next();

			p.setDataChanged(false);
		}
	}

	public boolean mayLeave() {
		boolean change = false;
		boolean result = false;

		Iterator<PanelGenEditTable> iterP = tablePanes.iterator();

		while (!change && iterP.hasNext()) {
			PanelGenEditTable p = iterP.next();
			change = p.isDataChanged();
		}

		if (change) {
			int returnedOption = JOptionPane.showOptionDialog(Utils.getMasterFrame(),
					Configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.text"),
					Configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				result = true;
			}

			Utils.getMasterFrame().setVisible(true);
		} else {
			result = true;
		}

		return result;
	}
}
