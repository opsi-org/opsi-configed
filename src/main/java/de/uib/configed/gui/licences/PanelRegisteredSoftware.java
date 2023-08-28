/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licences;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelRegisteredSoftware extends PanelGenEditTable {
	private ControlPanelAssignToLPools controller;

	private int[] saveRowSelection;

	public PanelRegisteredSoftware(ControlPanelAssignToLPools controller) {
		super("", 0, true, 2, true, new int[] { PanelGenEditTable.POPUP_RELOAD }, true);
		this.controller = controller;
		searchPane.setWithNavPane(true);
	}

	@Override
	public void reload() {
		super.reload();
		Logging.info(this, "reload");
		controller.setSoftwareIdsFromLicencePool();
		saveRowSelection = theTable.getSelectedRows();
	}

	@Override
	public void reset() {
		Logging.info(this, "reset");
		super.reset();

	}

	@Override
	public void commit() {
		Logging.info(this, "commit");
		super.commit();
		PersistenceControllerFactory.getPersistenceController().relationsAuditSoftwareToLicencePoolsRequestRefresh();
		super.reset();

		if (controller.getPanel().getFSoftwarename2LicencePool() != null) {
			Logging.info(this, "Panel.fSoftwarename2LicencePool.panelSWnames.reset");

			// does not solve the task
			controller.getPanel().getFSoftwarename2LicencePool().getPanelSWnames().reset();
		}

	}

	@Override
	public void cancel() {
		Logging.info(this, "cancel");
		super.cancel();

		controller.setSoftwareIdsFromLicencePool();
	}

	public void callName2Pool(int modelrow) {
		if (tableModel.getCursorRow() < 0) {
			return;
		}

		String nameVal = (String) tableModel.getValueAt(modelrow,
				getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));

		Logging.info(this, " got name " + nameVal);

		if (controller.getPanel().getFSoftwarename2LicencePool() != null) {
			controller.getPanel().getFSoftwarename2LicencePool().getPanelSWnames().moveToValue(nameVal, 0);
		}
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
	public void valueChanged(ListSelectionEvent e) {

		if (isAwareOfSelectionListener()) {
			Logging.debug(this, "selectionListener valueChanged, aware of selectionlistener");

		}

	}

	private boolean mouseInColumnOfMarkCursor(Point p) {
		int mouseCol = theTable.columnAtPoint(p);

		return mouseCol >= 0 && mouseCol == tableModel.getColMarkCursorRow();
	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		Point mousePoint = e.getPoint();
		int mouseRow = theTable.rowAtPoint(mousePoint);

		if (mouseInColumnOfMarkCursor(mousePoint)) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(mouseRow));
		} else {
			if (isAwareOfSelectionListener()) {
				Logging.info(this, "mouse click in table. outside colMarkCursorRow, aware of selectionlistener");

				controller.validateWindowsSoftwareKeys();

				if (controller.getPanel().getFSoftwarename2LicencePool().isVisible()) {
					Logging.info(this, "selectionListener valueChanged,fSoftwarename2LicencePool.isVisible ");

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
			Logging.info(this, "mouseReleased reset saveRowSelection ");

			if (saveRowSelection != null) {
				super.setSelection(saveRowSelection);
			}
		} else {
			saveRowSelection = theTable.getSelectedRows();
			Logging.info(this, "mouseReleased set new saveRowSelection ");
		}
	}

	@Override
	public void setDataChanged(boolean b) {
		if (b && controller.acknowledgeChangeForSWList()) {
			int col = theTable.getEditingColumn();
			Logging.info(this, "setDataChanged col " + col);
			if (tableModel.gotMarkCursorRow() && col != tableModel.getColMarkCursorRow()) {
				super.setDataChanged(true);
			}
		} else {
			super.setDataChanged(false);
		}
	}

	// CursorrowObserer
	@Override
	public void rowUpdated(int modelrow) {
		super.rowUpdated(modelrow);
		Logging.info(this, " rowUpdated to modelrow " + modelrow);
		callName2Pool(modelrow);
	}
}
