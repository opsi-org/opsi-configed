/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.uib.configed.gui.features.licenses.MultiTablePanel;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.share.AbstractDataChangedKeeper;
import de.uib.configed.share.Utils;

public abstract class AbstractControlMultiTablePanel {
	protected List<GenTableModel> tableModels = new ArrayList<>();

	protected List<PanelGenEdit> panelGenEdits = new ArrayList<>();

	protected List<MapBasedTableEditItem> updateCollection = new ArrayList<>();

	public abstract MultiTablePanel getTabClient();

	public List<GenTableModel> getTableModels() {
		return tableModels;
	}

	public List<PanelGenEdit> getPanelGenEdits() {
		return panelGenEdits;
	}

	public abstract void init();

	/**
	 * called by the MultiTablePanel reset method overwrite for the real content
	 */
	public void initializeVisualSettings() {
	}

	public void refreshPanelGenEdits() {
		for (GenTableModel tableModel : tableModels) {
			tableModel.invalidate();
			tableModel.reset();
		}

		for (PanelGenEdit tablePanel : panelGenEdits) {
			tablePanel.setDataChanged(false);
		}
	}

	public int mayLeave() {
		boolean change = panelGenEdits.stream().anyMatch(p -> p.isDataChanged());

		if (change) {
			return JOptionPane.showConfirmDialog(Utils.getMasterFrame(),
					Configed.getResourceValue("ConfigedMain.confirmUnsavedChanges"),
					Configed.getResourceValue("ConfigedMain.unsavedChanges"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		} else {
			return AbstractDataChangedKeeper.JOPTIONPANE_DIALOG_NOT_SHOWN;
		}
	}
}
