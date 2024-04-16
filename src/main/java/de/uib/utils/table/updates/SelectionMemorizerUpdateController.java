/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.updates;

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.PanelGenEditTable;

public class SelectionMemorizerUpdateController implements UpdateController {
	private PanelGenEditTable keysPanel;
	private int keyCol;
	private PanelGenEditTable panel;
	private ControlPanelAssignToLPools controlPanelAssignToLPools;

	public SelectionMemorizerUpdateController(PanelGenEditTable keysPanel, int keyCol, PanelGenEditTable panel,
			ControlPanelAssignToLPools controlPanelAssignToLPools) {
		this.keysPanel = keysPanel;
		this.keyCol = keyCol;
		this.panel = panel;
		this.controlPanelAssignToLPools = controlPanelAssignToLPools;
	}

	@Override
	public boolean saveChanges() {
		Logging.debug(this, "keysPanel is null " + (keysPanel == null));
		if (keysPanel.getSelectedRow() < 0) {
			Logging.info(this, "no row selected");

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.text"),
					Configed.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.title"),
					JOptionPane.OK_OPTION);

			return false;
		}

		String keyValue = keysPanel.getValueAt(keysPanel.getSelectedRow(), keyCol).toString();

		boolean success = controlPanelAssignToLPools.updateLicensepool(keyValue, panel.getSelectedKeys());

		Logging.checkErrorList(null);

		return success;
	}

	@Override
	public boolean cancelChanges() {
		controlPanelAssignToLPools.setSoftwareIdsFromLicensePool(null);
		return true;
	}
}
