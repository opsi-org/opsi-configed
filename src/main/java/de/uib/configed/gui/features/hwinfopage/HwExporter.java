/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class HwExporter implements ActionListener {
	private PanelHWMultiClientReport showHardwareLogMultiClientReport;
	private PanelHWSingleClientInfo panelHWInfo;
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public HwExporter(PanelHWMultiClientReport showHardwareLogMultiClientReport, PanelHWSingleClientInfo panelHWInfo,
			ConfigedMain configedMain) {
		this.showHardwareLogMultiClientReport = showHardwareLogMultiClientReport;
		this.panelHWInfo = panelHWInfo;
		this.configedMain = configedMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Configed.getSavedStates().setProperty("hwaudit_export_file_prefix",
				showHardwareLogMultiClientReport.getExportfilePrefix());

		String filepathStart = showHardwareLogMultiClientReport.getExportDirectory() + File.separator
				+ showHardwareLogMultiClientReport.getExportfilePrefix();

		String extension = "."
				+ showHardwareLogMultiClientReport.wantsKindOfExport().toString().toLowerCase(Locale.ROOT);

		panelHWInfo.setAskForOverwrite(showHardwareLogMultiClientReport.wantsAskForOverwrite());

		panelHWInfo.setKindOfExport(showHardwareLogMultiClientReport.wantsKindOfExport());

		for (String client : configedMain.getSelectedClients()) {
			panelHWInfo.updateContent(client);

			Map<String, List<Map<String, Object>>> hardwareInfo = persistenceController.getDataServices().hardware
					.getHardwareInfo(client);
			List<Map<String, Object>> scanProperty = hardwareInfo.get(PanelHWSingleClientInfo.SCANPROPERTYNAME);
			String scandate = scanProperty.get(0).get(PanelHWSingleClientInfo.SCANTIME).toString();
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
			panelHWInfo.setWriteToFile(filepath);
			panelHWInfo.export();
		}
	}
}
