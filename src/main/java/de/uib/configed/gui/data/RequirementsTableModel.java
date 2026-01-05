/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.data;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

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
		Logging.info(this, "creating");

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
		return switch (col) {
		case 0 -> Configed.getResourceValue("ProductInfoPane.RequirementsTable.requiredProduct");
		case 1 -> Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeDefault");
		case 2 -> Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeBefore");
		case 3 -> Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeAfter");
		default -> {
			Logging.warning(this, "no case found for col in getColumnName");
			yield "";
		}
		};
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
			case 1 -> {
				return getValueForCol1(myKey, rowTypeIndex);
			}
			case 2 -> {
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requBeforeMap != null) {
					result = requBeforeMap.get(myKey);
				}
			}
			case 3 -> {
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requAfterMap != null) {
					result = requAfterMap.get(myKey);
				}
			}
			default -> Logging.warning(this, "no case found for col in getValueAt");
			}

			if (result != null) {
				result = "(" + result + ")";
			}
		}

		return result;
	}

	private String getValueAtFirstColumn(int rowTypeIndex, String myKey) {
		final String IDENT = "     ";

		return switch (rowTypeIndex) {
		case 0 -> myKey;
		case 1 -> IDENT + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
				+ " setup";
		case 2 -> IDENT + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
				+ " uninstall";
		default -> {
			Logging.warning(this, "no case found for rowTypeIndex in getValueAt");
			yield null;
		}
		};
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
}
