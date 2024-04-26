/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.SensitiveCellEditor;

public class SensitiveCellEditorForDataPanel extends SensitiveCellEditor {
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// we use data panel :
		if (column == 1) {
			String key = "" + table.getValueAt(row, 0);
			if (Utils.isKeyForSecretValue(key)) {
				if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
						.isGlobalReadOnly()) {
					Logging.warning(this, Configed.getResourceValue("SensitiveCellEditor.editHiddenText.forbidden"));
					return null;
				}

				int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("SensitiveCellEditor.editHiddenText.text"),
						Configed.getResourceValue("SensitiveCellEditor.editHiddenText.title"),
						JOptionPane.YES_NO_OPTION);

				Logging.info(this,
						" getTableCellEditorComponent, celleditor working, returned option " + returnedOption);
				if (returnedOption != JOptionPane.YES_OPTION) {
					return null;
				}
			}
		}

		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
}
