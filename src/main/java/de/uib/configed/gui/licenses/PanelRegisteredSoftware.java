/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.PanelGenEditTable;

public class PanelRegisteredSoftware extends PanelGenEditTable implements MouseListener {
	private ControlPanelAssignToLPools controller;

	private int[] saveRowSelection;

	public PanelRegisteredSoftware(ControlPanelAssignToLPools controller) {
		super("", true, 2, new int[] { PanelGenEditTable.POPUP_RELOAD }, true);
		this.controller = controller;
		tableSearchPane.showNavPane();

		jTable.addMouseListener(this);
		jTable.getTableHeader().addMouseListener(this);
	}

	@Override
	public void reload() {
		super.reload();
		Logging.info(this, "reload");
		controller.setSoftwareIdsFromLicensePool();
		saveRowSelection = jTable.getSelectedRows();
	}

	@Override
	public void commit() {
		Logging.info(this, "commit");
		super.commit();
		PersistenceControllerFactory.getPersistenceController()
				.reloadData(ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD.toString());
		tableModel.reset();

		if (controller.getTabClient().getFSoftwarename2LicensePool() != null) {
			Logging.info(this, "Panel.fSoftwarename2LicensePool.panelSWnames.reset");

			// does not solve the task
			controller.getTabClient().getFSoftwarename2LicensePool().getPanelSWnames().getTableModel().reset();
		}
	}

	@Override
	public void cancel() {
		Logging.info(this, "cancel");
		super.cancel();

		controller.setSoftwareIdsFromLicensePool();
	}

	public void callName2Pool(int modelrow) {
		if (tableModel.getCursorRow() < 0) {
			return;
		}

		String nameVal = (String) tableModel.getValueAt(modelrow,
				getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));

		Logging.info(this, " got name ", nameVal);

		if (controller.getTabClient().getFSoftwarename2LicensePool() != null) {
			controller.getTabClient().getFSoftwarename2LicensePool().getPanelSWnames().moveToValue(nameVal, 0);
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
		saveRowSelection = jTable.getSelectedRows();
	}

	private boolean mouseInColumnOfMarkCursor(Point p) {
		int mouseCol = jTable.columnAtPoint(p);

		return mouseCol >= 0 && mouseCol == tableModel.getColMarkCursorRow();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point mousePoint = e.getPoint();
		int mouseRow = jTable.rowAtPoint(mousePoint);

		if (mouseInColumnOfMarkCursor(mousePoint)) {
			tableModel.setCursorRow(jTable.convertRowIndexToModel(mouseRow));
		} else if (isAwareOfSelectionListener()) {
			Logging.info(this, "mouse click in table. outside colMarkCursorRow, aware of selectionlistener");

			controller.validateWindowsSoftwareKeys();

			setDataChanged(true);
		} else {
			// Do nothing here on mouse click
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseInColumnOfMarkCursor(e.getPoint())) {
			Logging.info(this, "mouseReleased reset saveRowSelection ");

			if (saveRowSelection != null) {
				super.setSelection(saveRowSelection);
			}
		} else {
			saveRowSelection = jTable.getSelectedRows();
			Logging.info(this, "mouseReleased set new saveRowSelection ");
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Not needed here
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Not needed here
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Not needed here
	}

	@Override
	public void setDataChanged(boolean b) {
		if (b && controller.acknowledgeChangeForSWList()) {
			int col = jTable.getEditingColumn();
			Logging.info(this, "setDataChanged col ", col);
			if (tableModel.gotMarkCursorRow() && col != tableModel.getColMarkCursorRow()) {
				super.setDataChanged(true);
			}
		} else {
			super.setDataChanged(false);
		}
	}

	@Override
	public void rowUpdated(int modelrow) {
		super.rowUpdated(modelrow);
		Logging.info(this, " rowUpdated to modelrow ", modelrow);
		callName2Pool(modelrow);
	}
}
