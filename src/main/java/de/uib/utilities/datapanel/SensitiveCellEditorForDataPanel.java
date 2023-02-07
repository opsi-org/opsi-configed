/*
 * (c) uib, www.uib.de, 2016
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class SensitiveCellEditorForDataPanel extends de.uib.utilities.table.gui.SensitiveCellEditor {

	private static final Map<Object, SensitiveCellEditorForDataPanel> instances = new HashMap<>();

	public static synchronized SensitiveCellEditorForDataPanel getInstance(Object key) {

		// Zu key gehörige Instanz aus Map holen
		return instances.computeIfAbsent(key, arg -> {

			SensitiveCellEditorForDataPanel newInstance = new SensitiveCellEditorForDataPanel();
			newInstance.myKey = "" + key;
			Logging.debug(newInstance.getClass().getName() + " produced instance for key " + key
					+ " ; size of instances " + instances.size());
			return newInstance;
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// we use data panel :
		if (column == 1) {
			String key = "" + table.getValueAt(row, 0);
			if (Globals.isKeyForSecretValue(key)) {
				if (Globals.isGlobalReadOnly()) {
					Logging.warning(this, Configed.getResourceValue("SensitiveCellEditor.editHiddenText.forbidden"));
					return null;
				}

				int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("SensitiveCellEditor.editHiddenText.text"),
						Globals.APPNAME + " " + Configed.getResourceValue("SensitiveCellEditor.editHiddenText.title"),
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new Object[] { Configed.getResourceValue("SensitiveCellEditor.editHiddenText.yes"),
								Configed.getResourceValue("SensitiveCellEditor.editHiddenText.no"),
								Configed.getResourceValue("SensitiveCellEditor.editHiddenText.cancel") },
						JOptionPane.YES_OPTION);

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
