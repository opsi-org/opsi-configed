/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.commons.csv.CSVPrinter;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.features.csv.CSVImportDataModifier;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

public class ClientTableExporterToCSV extends ExporterToCSV {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Set<String> columnNames;

	public ClientTableExporterToCSV(JTable table) {
		super(table);
		this.columnNames = HostInfo.getKeysForCSV();
	}

	@Override
	protected void writeHeader(CSVPrinter printer) throws IOException {
		printer.printRecord(columnNames);
	}

	@Override
	protected void writeRows(CSVPrinter printer, boolean selectedOnly) throws IOException {
		Map<String, HostInfo> clientInfos = persistenceController.getDataServices().hostInfoCollections
				.getMapOfAllPCInfoMaps();
		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
			if (!theTable.isRowSelected(rowI) && selectedOnly) {
				continue;
			}

			// HostInfo clientInfo = clientInfos
			// 		.get(ConfigedMain.getMainFrame().getClientTablePanel().getClientTable().getClientName(rowI));
			// List<String> row = new ArrayList<>();
			// for (String columnName : columnNames) {
			// 	row.add(getRowValue(columnName, clientInfo));
			// }

			// printer.printRecord(row);
		}
	}

	private String getRowValue(String columnName, HostInfo clientInfo) {
		String clientName = clientInfo.getString(HostInfo.HOSTNAME_KEY);

		return switch (columnName) {
		case "id" -> clientName.substring(0, clientName.indexOf("."));
		case "domain" -> clientName.substring(clientName.indexOf(".") + 1);
		case "groups" -> getGroupsValue(clientName);
		default -> clientInfo.getMap().get(columnName).toString();
		};
	}

	private String getGroupsValue(String clientName) {
		Map<String, Set<String>> fObject2Groups = persistenceController.getDataServices().group.getFObject2GroupsPD();

		// We need to add an empty set if there are no groups
		if (fObject2Groups.containsKey(clientName)) {
			return String.join(",", fObject2Groups.get(clientName));
		} else {
			return "";
		}
	}

	@Override
	public JMenuItem getMenuItemExport() {
		JMenuItem menuItem = new JMenuItem(Configed.getResourceValue("ClientTableExporterToCSV.exportTableAsCSV"));
		Icons.addIntellijIconToMenuItem(menuItem, "export");
		menuItem.addActionListener((ActionEvent actionEvent) -> {
			columnNames = getColumnsToInclude();
			if (!columnNames.isEmpty()) {
				execute("client_report_", null, false);
			}
		});
		return menuItem;
	}

	@Override
	public JMenuItem getMenuItemExportSelected() {
		JMenuItem menuItem = new JMenuItem(
				Configed.getResourceValue("ClientTableExporterToCSV.exportSelectedRowsAsCSV"));
		Icons.addIntellijIconToMenuItem(menuItem, "export");

		menuItem.addActionListener((ActionEvent actionEvent) -> {
			Logging.debug(this, "menuItemExportSelectedCSV , only selected");
			columnNames = getColumnsToInclude();
			if (!columnNames.isEmpty()) {
				execute("client_report_", null, true);
			}
		});

		return menuItem;
	}

	private static Set<String> getColumnsToInclude() {
		ListSelectionDialog columnSelectionDialog = createColumnSelectionDialog();
		columnSelectionDialog.show();

		if (!columnSelectionDialog.wasAccepted()) {
			return Collections.emptySet();
		}

		Set<String> selected = new HashSet<>(columnSelectionDialog.getSelectedValues());
		List<String> important = CSVImportDataModifier.getImportantHeaders();
		LinkedHashSet<String> orderedResult = getOrderedSelectedColumns(selected, important);

		removeHostTypeKeyIfNeeded(orderedResult);

		return orderedResult;
	}

	private static ListSelectionDialog createColumnSelectionDialog() {
		ListSelectionDialog dialog = new ListSelectionDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ClientTableExporterToCSV.columnSelectionDialog.title"));
		Set<String> defaultValues = new LinkedHashSet<>(HostInfo.getKeysForCSV());
		dialog.setListData(new ArrayList<>(defaultValues));
		defaultValues.remove(HostInfo.HOST_KEY_KEY);
		dialog.setPreviousSelectionValues(defaultValues);
		dialog.setNonDeselectableValues(CSVImportDataModifier.getImportantHeaders());
		return dialog;
	}

	private static LinkedHashSet<String> getOrderedSelectedColumns(Set<String> selected, List<String> important) {
		LinkedHashSet<String> orderedResult = new LinkedHashSet<>();
		for (String key : HostInfo.getKeysForCSV()) {
			if (important.contains(key) || selected.contains(key)) {
				orderedResult.add(key);
			}
		}
		return orderedResult;
	}

	private static void removeHostTypeKeyIfNeeded(Set<String> columns) {
		if (columns.contains(HostInfo.HOST_TYPE_KEY) && !DialogUtils.includeOpsiHostKey()) {
			columns.remove(HostInfo.HOST_TYPE_KEY);
		}
	}
}
