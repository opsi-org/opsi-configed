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

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class SensitiveCellEditorForDataPanel extends de.uib.utilities.table.gui.SensitiveCellEditor {

	private static final Map<Object, SensitiveCellEditorForDataPanel> instances = new HashMap<Object, SensitiveCellEditorForDataPanel>();

	public static synchronized SensitiveCellEditorForDataPanel getInstance(Object key) {

		// Zu key gehörige Instanz aus Map holen
		SensitiveCellEditorForDataPanel instance = instances.get(key);

		if (instance == null) {
			// Lazy Creation, falls keine Instanz gefunden
			instance = new SensitiveCellEditorForDataPanel();
			// key.startsWith("secret")true);
			instances.put(key, instance);
			instance.myKey = "" + key;
			logging.debug(instance.getClass().getName() + " produced instance for key " + key + " ; size of instances "
					+ instances.size());
		}
		return instance;
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		// we use data panel :
		if (column == 1) {
			String key = "" + table.getValueAt(row, 0);
			if (Globals.isKeyForSecretValue(key)) {
				if (de.uib.configed.Globals.isGlobalReadOnly()) {
					logging.warning(this, configed.getResourceValue("SensitiveCellEditor.editHiddenText.forbidden"));
					return null;
				}

				int returnedOption = JOptionPane.NO_OPTION;

				returnedOption = JOptionPane.showOptionDialog(de.uib.configed.Globals.mainFrame,
						configed.getResourceValue("SensitiveCellEditor.editHiddenText.text"),
						Globals.APPNAME + " " + configed.getResourceValue("SensitiveCellEditor.editHiddenText.title"),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						new Object[] {
								configed.getResourceValue("SensitiveCellEditor.editHiddenText.yes"),
								configed.getResourceValue("SensitiveCellEditor.editHiddenText.no"),
								configed.getResourceValue("SensitiveCellEditor.editHiddenText.cancel")
						},
						JOptionPane.YES_OPTION);

				logging.info(this,
						" getTableCellEditorComponent, celleditor working, returned option " + returnedOption);
				if (returnedOption != JOptionPane.YES_OPTION) {
					return null;
				}
			}
		}

		return super.getTableCellEditorComponent(table,
				value,
				isSelected,
				row,
				column);
	}

}
