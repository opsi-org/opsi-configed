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
import de.uib.utils.table.gui.PanelGenEditTable;

public class PanelSoftwareLicencepool extends PanelGenEditTable {
	private ControlPanelAssignToLPools controlPanelAssignToLPools;
	private JButton buttonSetAllAssignmentsToPoolFromSelectedRow;
	private JLabel labelSetAllAssignmentsToPoolFromSelectedRow;

	private String labelText = Configed
			.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToPoolFromSelectedRow");

	public PanelSoftwareLicencepool(ControlPanelAssignToLPools controlPanelAssignToLPools,
			JButton buttonSetAllAssignmentsToPoolFromSelectedRow, JLabel labelSetAllAssignmentsToPoolFromSelectedRow) {
		super("", true, 0, new int[] { PanelGenEditTable.POPUP_RELOAD }, false);

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
		int selRow = jTable.getSelectedRow();
		if (selRow > -1) {
			val = getValueAt(selRow, 1);
		}

		if (val != null && jTable.getSelectedRowCount() == 1 && getTableModel().getRowCount() > 1
				&& !((String) val).equals(FSoftwarename2LicensePool.VALUE_NO_LICENSE_POOL)) {
			buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(true);
			labelSetAllAssignmentsToPoolFromSelectedRow
					.setText(labelText + " " + getValueAt(jTable.getSelectedRow(), 1));
		} else {
			buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
			labelSetAllAssignmentsToPoolFromSelectedRow.setText(labelText);
		}
	}
}
