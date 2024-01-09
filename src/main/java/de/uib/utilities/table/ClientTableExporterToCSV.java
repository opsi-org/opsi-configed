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

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.commons.csv.CSVPrinter;

import de.uib.configed.Configed;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ClientTableExporterToCSV extends ExporterToCSV {
	public ClientTableExporterToCSV(JTable table) {
		super(table);
	}

	@Override
	protected void writeHeader(CSVPrinter printer) throws IOException {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("hostname");
		columnNames.add("selectedDomain");
		columnNames.add("depotID");
		columnNames.add("description");
		columnNames.add("inventorynumber");
		columnNames.add("notes");
		columnNames.add("systemUUID");
		columnNames.add("macaddress");
		columnNames.add("ipaddress");
		columnNames.add("wanConfig");
		columnNames.add("uefiBoot");
		columnNames.add("shutdownInstall");
		printer.printRecord(columnNames);

	}

	@Override
	protected void writeRows(CSVPrinter printer, boolean selectedOnly) throws IOException {
		Map<String, HostInfo> clientInfos = PersistenceControllerFactory.getPersistenceController()
				.getHostInfoCollections().getMapOfAllPCInfoMaps();
		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
			if (!theTable.isRowSelected(rowI) && selectedOnly) {
				continue;
			}

			HostInfo clientInfo = clientInfos.get(theTable.getValueAt(rowI, 0));
			String clientName = clientInfo.getName();
			List<String> row = new ArrayList<>();
			row.add(clientName.substring(0, clientName.indexOf(".")));
			row.add(clientName.substring(clientName.indexOf(".") + 1, clientName.length()));
			row.add(clientInfo.getInDepot());
			row.add(clientInfo.getDescription());
			row.add(clientInfo.getInventoryNumber());
			row.add(clientInfo.getNotes());
			row.add(clientInfo.getSystemUUID());
			row.add(clientInfo.getMacAddress());
			row.add(clientInfo.getIpAddress());
			row.add(Boolean.toString(clientInfo.getWanConfig()));
			row.add(Boolean.toString(clientInfo.getUefiBoot()));
			row.add(Boolean.toString(clientInfo.getShutdownInstall()));
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
