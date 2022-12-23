/*
 * SelectionMemorizerUpdateController.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.thread.WaitCursor;

public abstract class SelectionMemorizerUpdateController implements de.uib.utilities.table.updates.UpdateController {
	PanelGenEditTable keysPanel;
	int keyCol;
	PanelGenEditTable panel;
	GenTableModel tablemodel;
	StrList2BooleanFunction updater;

	public SelectionMemorizerUpdateController(PanelGenEditTable keysPanel, int keyCol, PanelGenEditTable panel,
			GenTableModel tablemodel, StrList2BooleanFunction updater) {
		this.keysPanel = keysPanel;
		this.keyCol = keyCol;
		this.panel = panel;
		this.tablemodel = tablemodel;
		this.updater = updater;
	}

	@Override
	public boolean saveChanges() {
		boolean success = true;

		WaitCursor waitCursor = new WaitCursor();// Globals.masterFrame, this.getClass().getName() + ".saveChanges" );

		logging.debug(this, "keysPanel is null " + (keysPanel == null));
		if (keysPanel.getSelectedRow() < 0) {
			waitCursor.stop();
			logging.info(this, "no row selected");

			javax.swing.JOptionPane.showMessageDialog(Globals.mainContainer,
					de.uib.configed.configed
							.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.text"),
					Globals.APPNAME + "  "
							+ de.uib.configed.configed
									.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.title"),
					javax.swing.JOptionPane.OK_OPTION);

			return false;
		}

		String keyValue = keysPanel.getValueAt(keysPanel.getSelectedRow(), keyCol).toString();

		success = updater.sendUpdate(keyValue, panel.getSelectedKeys());

		waitCursor.stop();

		logging.checkErrorList(null);

		return success;
	}
}
