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

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.Configed;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.ColorTableCellRenderer;

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
		setActualProduct(null, null);
	}

	public void setActualProduct(String depotId, String product) {
		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;
		keyArray = zeroArray;

		if (product != null) {
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

		if (col == 0) {
			result = getValueAtFirstColumn(rowTypeIndex, myKey);
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

	private String getValueAtFirstColumn(int rowTypeIndex, String myKey) {
		String result = null;
		final String IDENT = "     ";

		switch (rowTypeIndex) {
		case 0:
			result = myKey;
			break;
		case 1:
			result = IDENT + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
					+ " setup";
			break;
		case 2:
			result = IDENT + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
					+ " uninstall";
			break;
		default:
			Logging.warning(this, "no case found for rowTypeIndex in getValueAt");
			break;
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
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			setToolTipText("" + value);

			// Don't warn in opsi 4.3, since now not only "setup" is allowed for dependencies
			if (!ServerFacade.isOpsi43()) {
				if (shouldWarn(column, String.valueOf(value))) {
					setIcon(Utils.createImageIcon("images/warning.png", "warning"));

					setToolTipText(Configed.getResourceValue("ProductInfoPane.RequirementsTable.warning"));
				} else {
					setIcon(null);
				}
			}

			return this;
		}

		private static boolean shouldWarn(int column, String cellValue) {
			return (column == 2 || column == 3)
					&& (cellValue.equals("(" + InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED) + ":)")
							|| cellValue.equals("(:" + ActionRequest.getLabel(ActionRequest.UNINSTALL) + ")"));
		}
	}
}
