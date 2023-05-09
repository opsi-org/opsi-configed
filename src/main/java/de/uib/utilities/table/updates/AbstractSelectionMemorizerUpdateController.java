/*
 * SelectionMemorizerUpdateController.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import javax.swing.JOptionPane;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.thread.WaitCursor;

public abstract class AbstractSelectionMemorizerUpdateController
		implements de.uib.utilities.table.updates.UpdateController {
	private PanelGenEditTable keysPanel;
	private int keyCol;
	private PanelGenEditTable panel;
	private StrList2BooleanFunction updater;

	protected AbstractSelectionMemorizerUpdateController(PanelGenEditTable keysPanel, int keyCol,
			PanelGenEditTable panel, StrList2BooleanFunction updater) {
		this.keysPanel = keysPanel;
		this.keyCol = keyCol;
		this.panel = panel;
		this.updater = updater;
	}

	@Override
	public boolean saveChanges() {

		WaitCursor waitCursor = new WaitCursor();

		Logging.debug(this, "keysPanel is null " + (keysPanel == null));
		if (keysPanel.getSelectedRow() < 0) {
			waitCursor.stop();
			Logging.info(this, "no row selected");

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					de.uib.configed.Configed
							.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.text"),
					Globals.APPNAME + "  "
							+ de.uib.configed.Configed
									.getResourceValue("SelectionMemorizerUpdateController.no_row_selection.title"),
					JOptionPane.OK_OPTION);

			return false;
		}

		String keyValue = keysPanel.getValueAt(keysPanel.getSelectedRow(), keyCol).toString();

		boolean success = updater.sendUpdate(keyValue, panel.getSelectedKeys());

		waitCursor.stop();

		Logging.checkErrorList(null);

		return success;
	}
}
