/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.type.ConfigOption;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class HostConfigTable extends JTable {
	private Map<String, Object> defaultsMap;
	private List<String> propertyNames;
	private Map<String, Object> originalMap;
	private Map<String, String> descriptionsMap;
	private boolean includeAdditionalTooltipText;

	private Map<String, ConfigOption> serverConfigs = PersistenceControllerFactory.getPersistenceController()
			.getConfigDataService().getConfigOptionsPD();

	public HostConfigTable(MapTableModel mapTableModel, Map<String, Object> defaultsMap, List<String> propertyNames,
			Map<String, Object> originalMap, Map<String, String> descriptionsMap,
			boolean includeAdditionalTooltipText) {
		super(mapTableModel);

		this.defaultsMap = defaultsMap;
		this.propertyNames = propertyNames;
		this.originalMap = originalMap;
		this.descriptionsMap = descriptionsMap;
		this.includeAdditionalTooltipText = includeAdditionalTooltipText;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (c instanceof JComponent) {
			JComponent jc = (JComponent) c;

			String propertyName = propertyNames.get(rowIndex);

			jc.setToolTipText("<html>" + createTooltipForPropertyName(propertyName) + "</html>");

			// check equals with default

			Object defaultValue;

			if (defaultsMap == null) {
				Logging.warning(this, "no default values available, defaultsMap is null");
			} else if ((defaultValue = defaultsMap.get(getValueAt(rowIndex, 0))) == null) {
				Logging.warning(this, "no default Value found");

				jc.setForeground(Globals.OPSI_ERROR);

				jc.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

				jc.setFont(jc.getFont().deriveFont(Font.BOLD));
			} else if (!defaultValue.equals(getValueAt(rowIndex, 1))
					|| (originalMap != null && originalMap.containsKey(propertyName))) {
				jc.setFont(jc.getFont().deriveFont(Font.BOLD));
			} else {
				// Do nothing, since it's defaultvalue
			}

			setText(jc, vColIndex, rowIndex);
		}
		return c;
	}

	private void setText(JComponent jc, int vColIndex, int rowIndex) {
		if (vColIndex == 1 && Utils.isKeyForSecretValue((String) getValueAt(rowIndex, 0))) {
			if (jc instanceof JLabel) {
				((JLabel) jc).setText(Globals.STARRED_STRING);
			} else if (jc instanceof JTextComponent) {
				((JTextComponent) jc).setText(Globals.STARRED_STRING);
			} else {
				// Do nothing
			}
		}
	}

	private String createTooltipForPropertyName(String propertyName) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {

			if (includeAdditionalTooltipText) {
				tooltip.append("default (" + getPropertyOrigin(propertyName) + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (Utils.isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append("<br/><br/>" + descriptionsMap.get(propertyName));
		}

		return tooltip.toString();
	}

	private String getPropertyOrigin(String propertyName) {
		if (serverConfigs != null && serverConfigs.containsKey(propertyName)
				&& !serverConfigs.get(propertyName).getDefaultValues().equals(defaultsMap.get(propertyName))) {
			return "depot";
		} else {
			return "server";
		}
	}
}
