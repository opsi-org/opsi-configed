/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.licenses;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.ControlPanelAssignToLPools;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.logging.Logging;

public class PanelRegisteredSoftware extends PanelGenEdit implements MouseListener {
	private ControlPanelAssignToLPools controller;

	private int[] saveRowSelection;

	public PanelRegisteredSoftware(ControlPanelAssignToLPools controller) {
		super("", true, 2, new int[] { PopupMenuTrait.POPUP_RELOAD }, true);
		this.controller = controller;
		tableSearchPane.showNavPane();

		genEditTable.addMouseListener(this);
		genEditTable.getTableHeader().addMouseListener(this);
	}

	@Override
	public void reload() {
		super.reload();
		Logging.info(this, "reload");
		controller.setSoftwareIdsFromLicensePool();
		saveRowSelection = genEditTable.getSelectedRows();
	}

	@Override
	public void commit() {
		Logging.info(this, "commit");
		super.commit();
		PersistenceControllerFactory.getPersistenceController()
				.reloadData(ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD.toString());
		genEditTable.getGenTableModel().reset();

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
		if (genEditTable.getGenTableModel().getCursorRow() < 0) {
			return;
		}

		String nameVal = (String) genEditTable.getValueAt(genEditTable.convertRowIndexToView(modelrow),
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
		saveRowSelection = genEditTable.getSelectedRows();
	}

	private boolean mouseInColumnOfMarkCursor(Point p) {
		int mouseCol = genEditTable.columnAtPoint(p);

		return mouseCol >= 0 && mouseCol == genEditTable.getGenTableModel().getColMarkCursorRow();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point mousePoint = e.getPoint();
		int mouseRow = genEditTable.rowAtPoint(mousePoint);

		if (mouseInColumnOfMarkCursor(mousePoint)) {
			genEditTable.getGenTableModel().setCursorRow(genEditTable.convertRowIndexToModel(mouseRow));
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
			saveRowSelection = genEditTable.getSelectedRows();
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
			int col = genEditTable.getEditingColumn();
			Logging.info(this, "setDataChanged col ", col);
			if (genEditTable.getGenTableModel().gotMarkCursorRow()
					&& col != genEditTable.getGenTableModel().getColMarkCursorRow()) {
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
