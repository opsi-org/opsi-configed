/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo;
import de.uib.configed.gui.type.SWAuditClientEntry;
import de.uib.configed.share.logging.Logging;

public class SwExporter implements ActionListener {
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	private PanelSWSingleClientInfo panelSWInfo;
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SwExporter(PanelSWMultiClientReport showSoftwareLogMultiClientReport, PanelSWSingleClientInfo panelSWInfo,
			ConfigedMain configedMain) {
		this.showSoftwareLogMultiClientReport = showSoftwareLogMultiClientReport;
		this.panelSWInfo = panelSWInfo;
		this.configedMain = configedMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Logging.info(this, "actionPerformed ", "  showSoftwareLog_MultiClientReport.wantsWithMsUpdates  ",
				showSoftwareLogMultiClientReport.wantsWithMsUpdates());

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

		for (String client : configedMain.getSelectedClients()) {
			panelSWInfo.setHost(client);
			panelSWInfo.updateModel();

			Map<String, List<SWAuditClientEntry>> swAuditClientEntries = persistenceController
					.getDataServices().software.getSoftwareAuditOnClients(List.of(client));
			String scandate = persistenceController.getDataServices().software
					.getLastSoftwareAuditModification(swAuditClientEntries, client);
			if (scandate != null) {
				int timePos = scandate.indexOf(' ');
				if (timePos >= 0) {
					scandate = scandate.substring(0, timePos);
				} else {
					scandate = "__";
				}
			}

			String filepath = filepathStart + client + "__scan_" + scandate + extension;
			Logging.debug(this, "actionPerformed, write to ", filepath);
			panelSWInfo.setWriteToFile(filepath);
			panelSWInfo.export();
		}
	}
}
