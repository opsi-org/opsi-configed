/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SwExporter implements ActionListener {
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	private PanelSWInfo panelSWInfo;
	private ConfigedMain configedMain;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SwExporter(PanelSWMultiClientReport showSoftwareLogMultiClientReport, PanelSWInfo panelSWInfo,
			ConfigedMain configedMain) {
		this.showSoftwareLogMultiClientReport = showSoftwareLogMultiClientReport;
		this.panelSWInfo = panelSWInfo;
		this.configedMain = configedMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Logging.info(this, "actionPerformed " + "  showSoftwareLog_MultiClientReport.wantsWithMsUpdates  "
				+ showSoftwareLogMultiClientReport.wantsWithMsUpdates());

		// save states now

		Configed.getSavedStates().setProperty("swaudit_export_file_prefix",
				showSoftwareLogMultiClientReport.getExportfilePrefix());

		String filepathStart = showSoftwareLogMultiClientReport.getExportDirectory() + File.separator
				+ showSoftwareLogMultiClientReport.getExportfilePrefix();

		String extension = "."
				+ showSoftwareLogMultiClientReport.wantsKindOfExport().toString().toLowerCase(Locale.ROOT);

		panelSWInfo.setWithMsUpdates(showSoftwareLogMultiClientReport.wantsWithMsUpdates());
		panelSWInfo.setWithMsUpdates2(showSoftwareLogMultiClientReport.wantsWithMsUpdates2());

		panelSWInfo.setAskForOverwrite(showSoftwareLogMultiClientReport.wantsAskForOverwrite());

		panelSWInfo.setKindOfExport(showSoftwareLogMultiClientReport.wantsKindOfExport());

		List<String> clientsWithoutScan = new ArrayList<>();

		for (String client : configedMain.getSelectedClients()) {
			Map<String, Map<String, Object>> tableData = persistenceController.retrieveSoftwareAuditData(client);
			if (tableData == null || tableData.isEmpty()) {
				clientsWithoutScan.add(client);
			}
		}

		Logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

		for (String client : configedMain.getSelectedClients()) {
			panelSWInfo.setHost(client);

			panelSWInfo.updateModel();

			String scandate = persistenceController.getLastSoftwareAuditModification(client);
			if (scandate != null) {
				int timePos = scandate.indexOf(' ');
				if (timePos >= 0) {
					scandate = scandate.substring(0, timePos);
				} else {
					scandate = "__";
				}
			}

			String filepath = filepathStart + client + "__scan_" + scandate + extension;
			Logging.debug(this, "actionPerformed, write to " + filepath);
			panelSWInfo.setWriteToFile(filepath);

			panelSWInfo.export();
		}

		Logging.info(this, "clientsWithoutScan " + clientsWithoutScan);
	}
}
