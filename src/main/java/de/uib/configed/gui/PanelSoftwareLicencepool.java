/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.gui.PanelGenEdit;

public class PanelSoftwareLicencepool extends PanelGenEdit {
	private ControlPanelAssignToLPools controlPanelAssignToLPools;
	private JButton buttonSetAllAssignmentsToPoolFromSelectedRow;
	private JLabel labelSetAllAssignmentsToPoolFromSelectedRow;

	private String labelText = Configed
			.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToPoolFromSelectedRow");

	public PanelSoftwareLicencepool(ControlPanelAssignToLPools controlPanelAssignToLPools,
			JButton buttonSetAllAssignmentsToPoolFromSelectedRow, JLabel labelSetAllAssignmentsToPoolFromSelectedRow) {
		super("", true, 0, new int[] { PopupMenuTrait.POPUP_RELOAD }, false);

		this.controlPanelAssignToLPools = controlPanelAssignToLPools;
		this.buttonSetAllAssignmentsToPoolFromSelectedRow = buttonSetAllAssignmentsToPoolFromSelectedRow;
		this.labelSetAllAssignmentsToPoolFromSelectedRow = labelSetAllAssignmentsToPoolFromSelectedRow;
	}

	@Override

	public void commit() {
		super.commit();

		controlPanelAssignToLPools.setSoftwareIdsFromLicensePool();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.info(this, "panelSWxLicensepool ListSelectionEvent ", e);
		super.valueChanged(e);
		if (e.getValueIsAdjusting()) {
			return;
		}

		Object val = null;
		int selRow = genEditTable.getSelectedRow();
		if (selRow > -1) {
			val = getValueAt(selRow, 1);
		}

		if (val != null && genEditTable.getSelectedRowCount() == 1 && getTableModel().getRowCount() > 1
				&& !((String) val).equals(Softwarename2LicensePoolDialog.VALUE_NO_LICENSE_POOL)) {
			buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(true);
			labelSetAllAssignmentsToPoolFromSelectedRow
					.setText(labelText + " " + getValueAt(genEditTable.getSelectedRow(), 1));
		} else {
			buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
			labelSetAllAssignmentsToPoolFromSelectedRow.setText(labelText);
		}
	}
}
