/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.share.logging.Logging;

public class ProductTableExtractor {
	GenericTableViewComponent tableViewComponent;

	public ProductTableExtractor(GenericTableViewComponent tableViewComponent) {
		this.tableViewComponent = tableViewComponent;
	}

	public JTable getStrippedTable() {
		List<String[]> data = new ArrayList<>();
		List<TableColumnConfig> columns = tableViewComponent.getVisibleColumns();
		List<RowData> rows = tableViewComponent.getRows();

		for (int j = 0; j < rows.size(); j++) {
			RowData rowData = rows.get(j);
			boolean strippIt = true;
			String[] actCol = new String[columns.size()];

			for (int i = 0; i < columns.size(); i++) {
				TableColumnConfig columnConfig = columns.get(i);
				Object cellValue = rowData.getValue(columnConfig.getKey(), Object.class);
				String cellValueString = cellValue == null ? "" : cellValue.toString();
				actCol[i] = cellValueString;

				strippIt = shouldStripIt(columnConfig.getHeader(), cellValueString, strippIt);
			}

			if (!strippIt) {
				data.add(actCol);
			}
		}

		// Create jTable with stripped data
		int rowCount = data.size();
		int colCount = columns.size();
		String[][] strippedData = new String[rowCount][colCount];
		for (int i = 0; i < data.size(); i++) {
			strippedData[i] = data.get(i);
		}

		return new JTable(strippedData, extractHeaders(columns));
	}

	private static String[] extractHeaders(List<TableColumnConfig> columns) {
		String[] headers = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			headers[i] = columns.get(i).getHeader();
		}
		return headers;
	}

	private boolean shouldStripIt(String columnName, String cellValueString, boolean previousValue) {
		String installationStatusLabel = Configed.getResourceValue("InstallationStateTableModel.installationStatus");
		String reportLabel = Configed.getResourceValue("InstallationStateTableModel.report");
		String actionRequestLabel = Configed.getResourceValue("InstallationStateTableModel.actionRequest");

		boolean strippIt = previousValue;

		if (installationStatusLabel.equals(columnName)
				&& !InstallationStatus.KEY_NOT_INSTALLED.equals(cellValueString)) {
			strippIt = false;
		} else if (reportLabel.equals(columnName) && cellValueString != null && !cellValueString.isEmpty()) {
			strippIt = false;
		} else if (actionRequestLabel.equals(columnName) && !"none".equals(cellValueString)) {
			strippIt = false;
		} else {
			// Keep row if we don't have explicit strip rules for this column
			// This maintains backward compatibility with the warning log behavior
			Logging.debug(this, "checking strip condition for columnName: ", columnName);
		}

		return strippIt;
	}
}
