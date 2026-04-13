/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.updates;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ControlPanelAssignToLPools;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.share.logging.Logging;

public class SelectionMemorizerUpdateController implements UpdateController {
	private PanelGenEdit keysPanel;
	private int keyCol;
	private PanelGenEdit panel;
	private ControlPanelAssignToLPools controlPanelAssignToLPools;

	public SelectionMemorizerUpdateController(PanelGenEdit keysPanel, int keyCol, PanelGenEdit panel,
			ControlPanelAssignToLPools controlPanelAssignToLPools) {
		this.keysPanel = keysPanel;
		this.keyCol = keyCol;
		this.panel = panel;
		this.controlPanelAssignToLPools = controlPanelAssignToLPools;
	}

	@Override
	public boolean saveChanges() {
		Logging.debug(this, "keysPanel is null ", keysPanel == null);
		if (keysPanel.getGenEditTable().getSelectedRow() < 0) {
			Logging.info(this, "no row selected");

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.text"),
					Configed.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.title"),
					JOptionPane.OK_OPTION);

			return false;
		}

		String keyValue = keysPanel.getValueAt(keysPanel.getGenEditTable().getSelectedRow(), keyCol).toString();

		boolean success = controlPanelAssignToLPools.updateLicensepool(keyValue, panel.getSelectedKeys());

		Logging.checkErrorList();

		return success;
	}

	@Override
	public boolean cancelChanges() {
		controlPanelAssignToLPools.setSoftwareIdsFromLicensePool(null);
		return true;
	}
}
