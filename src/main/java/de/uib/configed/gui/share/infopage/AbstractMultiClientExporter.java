/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.infopage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;
import de.uib.configed.share.logging.Logging;

@SuppressWarnings("java:S103")
public abstract class AbstractMultiClientExporter<T extends AbstractSingleClientInfoPanel, V extends BaseMultiClientReportPanel>
		implements ActionListener {
	protected T panelInfo;
	protected V reportPanel;
	protected ConfigedMain configedMain;

	protected AbstractMultiClientExporter(T panelInfo, V reportPanel, ConfigedMain configedMain) {
		this.panelInfo = panelInfo;
		this.reportPanel = reportPanel;
		this.configedMain = configedMain;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String exportDirectory = reportPanel.getExportDirectory();
		if (exportDirectory == null || exportDirectory.isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.message"),
					Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Configed.getSavedStates().setProperty(getExportPrefixKey(), reportPanel.getExportfilePrefix());

		String filepathStart = exportDirectory + File.separator + reportPanel.getExportfilePrefix();
		String extension = "." + reportPanel.wantsKindOfExport().toString().toLowerCase(Locale.ROOT);

		panelInfo.setAskForOverwrite(reportPanel.wantsAskForOverwrite());
		panelInfo.setKindOfExport(reportPanel.wantsKindOfExport());
		applyExtraSettings();

		for (String client : configedMain.getSelectedClients()) {
			updatePanelForClient(client);

			String scanDate = getScanDateForClient(client);
			if (scanDate == null) {
				scanDate = "__";
			} else {
				int spaceIndex = scanDate.indexOf(' ');
				if (spaceIndex >= 0) {
					scanDate = scanDate.substring(0, spaceIndex);
				}
			}

			String filepath = filepathStart + client + "__scan_" + scanDate + extension;
			Logging.debug(this, "actionPerformed, write to ", filepath);
			panelInfo.setWriteToFile(filepath);
			panelInfo.getSingleClientExporter().export();
		}
	}

	protected abstract String getExportPrefixKey();

	protected abstract void applyExtraSettings();

	protected abstract void updatePanelForClient(String client);

	protected abstract String getScanDateForClient(String client);
}
