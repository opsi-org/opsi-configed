/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.commons.csv.CSVPrinter;

import de.uib.configed.Configed;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ClientTableExporterToCSV extends ExporterToCSV {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private List<String> columnNames;

	public ClientTableExporterToCSV(JTable table, List<String> columnNames) {
		super(table);
		this.columnNames = columnNames;
	}

	@Override
	protected void writeHeader(CSVPrinter printer) throws IOException {
		printer.printRecord(columnNames);
	}

	@Override
	protected void writeRows(CSVPrinter printer, boolean selectedOnly) throws IOException {
		Map<String, HostInfo> clientInfos = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps();
		Map<String, Set<String>> fObject2Groups = persistenceController.getGroupDataService().getFObject2GroupsPD();
		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
			if (!theTable.isRowSelected(rowI) && selectedOnly) {
				continue;
			}

			HostInfo clientInfo = clientInfos.get(theTable.getValueAt(rowI, 0));
			Map<String, Object> clientInfoMap = clientInfo.getMap();
			List<String> row = new ArrayList<>();
			for (String columnName : columnNames) {
				String clientName = clientInfo.getName();
				if ("id".equals(columnName)) {
					row.add(clientName.substring(0, clientName.indexOf(".")));
				} else if ("domain".equals(columnName)) {
					row.add(clientName.substring(clientName.indexOf(".") + 1, clientName.length()));
				} else if ("group".equals(columnName)) {
					row.add(String.join(",", fObject2Groups.get(clientName)));
				} else if (clientInfoMap.get(columnName) instanceof Boolean) {
					row.add(Boolean.toString((Boolean) clientInfoMap.get(columnName)));
				} else {
					row.add((String) clientInfoMap.get(columnName));
				}
			}

			if (!row.isEmpty()) {
				printer.printRecord(row);
			}
		}
	}

	@Override
	public JMenuItem getMenuItemExport() {
		JMenuItem menuItem = new JMenuItem(Configed.getResourceValue("ClientTableExporterToCSV.exportTableAsCSV"));
		menuItem.addActionListener(actionEvent -> execute(null, false));
		return menuItem;
	}

	@Override
	public JMenuItem getMenuItemExportSelected() {
		JMenuItem menuItem = new JMenuItem(
				Configed.getResourceValue("ClientTableExporterToCSV.exportSelectedRowsAsCSV"));

		menuItem.addActionListener((ActionEvent actionEvent) -> {
			boolean onlySelected = true;
			Logging.debug(this, "menuItemExportSelectedCSV " + onlySelected);
			execute(null, onlySelected);
		});

		return menuItem;
	}
}
