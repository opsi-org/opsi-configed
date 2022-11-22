/* 
 * Copyright (C) 2009 uib.de
 *
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;

public abstract class ControlMultiTablePanel {
	protected List<GenTableModel> tableModels = new ArrayList<>();

	protected List<PanelGenEditTable> tablePanes = new ArrayList<>();

	protected de.uib.utilities.table.updates.TableUpdateCollection updateCollection = new de.uib.utilities.table.updates.TableUpdateCollection();

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
			int returnedOption = JOptionPane.showOptionDialog(Globals.frame1,
					configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.text"),
					configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION)
				result = true;

			Globals.frame1.setVisible(true);
		} else
			result = true;

		return result;
	}

}
