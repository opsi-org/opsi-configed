/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.awt.Component;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import utils.Utils;

public class RequirementsTableModel extends AbstractTableModel {

	private static final int NO_OF_ROW_TYPES = 3;

	private Object[] keyArray;
	private final Object[] zeroArray = new Object[] {};

	private Map<String, String> requMap;
	private Map<String, String> requBeforeMap;
	private Map<String, String> requAfterMap;
	private Map<String, String> requDeinstallMap;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public RequirementsTableModel() {
		Logging.info(this.getClass(), "creating");

		init();
	}

	private void retrieveRequirements(String depotId, String product) {
		// if depotId == null the depot representative is used
		requMap = persistenceController.getProductDataService().getProductRequirements(depotId, product);
		requBeforeMap = persistenceController.getProductDataService().getProductPreRequirements(depotId, product);
		requAfterMap = persistenceController.getProductDataService().getProductPostRequirements(depotId, product);
		requDeinstallMap = persistenceController.getProductDataService().getProductDeinstallRequirements(depotId,
				product);
	}

	private void init() {
		// we assume that the productId determines the requirements since we are on a
		// preselected depot
		setActualProduct(null, "");
	}

	public void setActualProduct(String depotId, String product) {

		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;
		keyArray = zeroArray;

		if (product != null && !product.trim().isEmpty()) {
			retrieveRequirements(depotId, product);

			Set<String> keySet = new TreeSet<>();
			if (requMap != null && requMap.keySet() != null) {
				keySet.addAll(requMap.keySet());
			}
			if (requBeforeMap != null && requBeforeMap.keySet() != null) {
				keySet.addAll(requBeforeMap.keySet());
			}
			if (requAfterMap != null && requAfterMap.keySet() != null) {
				keySet.addAll(requAfterMap.keySet());
			}
			if (requDeinstallMap != null && requDeinstallMap.keySet() != null) {
				keySet.addAll(requDeinstallMap.keySet());
			}
			keyArray = keySet.toArray();
		}

		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return keyArray.length * NO_OF_ROW_TYPES;
	}

	@Override
	public String getColumnName(int col) {
		String result = "";
		switch (col) {
		case 0:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requiredProduct");
			break;

		case 1:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeDefault");
			break;
		case 2:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeBefore");
			break;
		case 3:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeAfter");
			break;
		default:
			Logging.warning(this, "no case found for col in getColumnName");
			break;
		}

		return result;
	}

	@Override
	public Object getValueAt(int row, int col) {

		String myKey = (String) keyArray[row / NO_OF_ROW_TYPES];

		int rowTypeIndex = row % NO_OF_ROW_TYPES;

		Object result = null;
		String indent = "     ";

		if (col == 0) {
			switch (rowTypeIndex) {

			case 0:
				result = myKey;
				break;
			case 1:
				result = indent + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " setup";
				break;
			case 2:
				result = indent + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " uninstall";
				break;
			default:
				Logging.warning(this, "no case found for rowTypeIndex in getValueAt");
				break;
			}
		} else {
			switch (col) {
			case 1:
				return getValueForCol1(myKey, rowTypeIndex);

			case 2:
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requBeforeMap != null) {
					result = requBeforeMap.get(myKey);
				}
				break;

			case 3:
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requAfterMap != null) {
					result = requAfterMap.get(myKey);
				}
				break;

			default:
				Logging.warning(this, "no case found for col in getValueAt");
				break;
			}

			if (result != null) {
				result = "(" + result + ")";
			}
		}

		return result;
	}

	private String getValueForCol1(String myKey, int rowTypeIndex) {
		if (rowTypeIndex == 1 && requMap != null) {
			return requMap.get(myKey);
		} else if (rowTypeIndex == 2 && requDeinstallMap != null) {
			return requDeinstallMap.get(myKey);
		} else {
			return null;
		}
	}

	public MyTableCellRenderer getTableCellRenderer() {
		return new MyTableCellRenderer();
	}

	private static final class MyTableCellRenderer extends ColorTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (cell instanceof JComponent) {
				((JComponent) cell).setToolTipText("" + value);
			}

			// warning
			String cellValue = String.valueOf(value);
			if ((column == 2 || column == 3)
					&& (cellValue.equals("(" + InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED) + ":)")
							|| cellValue.equals("(:" + ActionRequest.getLabel(ActionRequest.UNINSTALL) + ")"))) {

				((JLabel) cell).setIcon(Utils.createImageIcon("images/warning.png", "warning"));

				((JLabel) cell).setHorizontalTextPosition(SwingConstants.LEADING);

				((JLabel) cell).setToolTipText(Configed.getResourceValue("ProductInfoPane.RequirementsTable.warning"));

				return cell;
			} else {
				((JLabel) cell).setIcon(null);
			}

			return cell;
		}
	}
}
