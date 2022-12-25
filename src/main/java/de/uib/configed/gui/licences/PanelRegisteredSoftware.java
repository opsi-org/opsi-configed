
package de.uib.configed.gui.licences;

import java.awt.event.MouseEvent;
import java.util.List;

import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * Copyright (C) 2020 uib.de
 * 
 * @author roeder
 */
public class PanelRegisteredSoftware extends PanelGenEditTable {
	ControlPanelAssignToLPools controller;

	int[] saveRowSelection;

	public PanelRegisteredSoftware(ControlPanelAssignToLPools controller) {
		super("",

				0, true, 2, true, new int[] { PanelGenEditTable.POPUP_RELOAD }, true

		);
		this.controller = controller;
		searchPane.setWithNavPane(true);
	}

	@Override
	public void reload() {
		super.reload();
		logging.info(this, "reload");
		((ControlPanelAssignToLPools) controller).setSoftwareIdsFromLicencePool();
		saveRowSelection = theTable.getSelectedRows();
	}

	@Override
	public void reset() {
		logging.info(this, "reset");
		super.reset();

	}

	@Override
	public void commit() {
		logging.info(this, "commit");
		super.commit();
		((ControlPanelAssignToLPools) controller).persist.relations_auditSoftwareToLicencePools_requestRefresh();
		super.reset();

		if (controller.thePanel.fSoftwarename2LicencePool != null) {
			logging.info(this, "Panel.fSoftwarename2LicencePool.panelSWnames.reset");

			// does not solve the task
			controller.thePanel.fSoftwarename2LicencePool.panelSWnames.reset();
		}

	}

	@Override
	public void cancel() {
		logging.info(this, "cancel");
		super.cancel();

		((ControlPanelAssignToLPools) controller).setSoftwareIdsFromLicencePool();
	}

	public void callName2Pool(int modelrow) {
		if (tableModel.getCursorRow() < 0)
			return;

		String nameVal = (String) tableModel.getValueAt(modelrow,
				getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));

		logging.info(this, " got name " + nameVal);

		if (controller.thePanel.fSoftwarename2LicencePool != null)
			controller.thePanel.fSoftwarename2LicencePool.panelSWnames.moveToValue(nameVal, 0);
	}

	@Override
	public void setSelection(int[] selection) {
		super.setSelection(selection);
		saveRowSelection = selection;
	}

	@Override
	public void setSelectedValues(List<String> values, int col) {
		super.setSelectedValues(values, col);
		saveRowSelection = theTable.getSelectedRows();
	}

	// ListSelectionListener
	@Override
	public void valueChanged(javax.swing.event.ListSelectionEvent e) {

		if (isAwareOfSelectionListener()) {
			logging.debug(this, "selectionListener valueChanged, aware of selectionlistener");

		}

	}

	private boolean mouseInColumnOfMarkCursor(java.awt.Point p) {
		int mouseCol = theTable.columnAtPoint(p);
		if (mouseCol >= 0 && mouseCol == tableModel.getColMarkCursorRow())
			return true;

		return false;
	}

	// MouseListener

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		java.awt.Point mousePoint = e.getPoint();
		int mouseRow = theTable.rowAtPoint(mousePoint);

		if (mouseInColumnOfMarkCursor(mousePoint)) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(mouseRow));
		} else {
			if (isAwareOfSelectionListener()) {
				logging.info(this, "mouse click in table. outside colMarkCursorRow, aware of selectionlistener");

				((ControlPanelAssignToLPools) controller).validateWindowsSoftwareKeys();

				if (controller.thePanel.fSoftwarename2LicencePool.isVisible()) {
					logging.info(this, "selectionListener valueChanged,fSoftwarename2LicencePool.isVisible ");

					// the data is not refreshed
				}

				setDataChanged(true);

			}
		}

		super.mouseClicked(e);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseInColumnOfMarkCursor(e.getPoint())) {
			logging.info(this, "mouseReleased reset saveRowSelection ");

			if (saveRowSelection != null)
				super.setSelection(saveRowSelection);

		}

		else {
			saveRowSelection = theTable.getSelectedRows();
			logging.info(this, "mouseReleased set new saveRowSelection ");
		}
	}

	@Override
	public void setDataChanged(boolean b) {
		if (b && ((ControlPanelAssignToLPools) controller).acknowledgeChangeForSWList()) {
			int col = theTable.getEditingColumn();
			logging.info(this, "setDataChanged col " + col);
			if (tableModel.gotMarkCursorRow() && col != tableModel.getColMarkCursorRow()) {
				super.setDataChanged(true);
			}
		} else
			super.setDataChanged(false);

	}

	// CursorrowObserer
	@Override
	public void rowUpdated(int modelrow) {
		super.rowUpdated(modelrow);
		logging.info(this, " rowUpdated to modelrow " + modelrow);
		callName2Pool(modelrow);
	}

}
