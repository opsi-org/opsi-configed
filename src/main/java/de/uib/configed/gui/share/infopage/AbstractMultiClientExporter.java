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
import java.util.Set;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;
import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
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
			showMissingDirectoryMessage();
			return;
		}

		String prefix = reportPanel.getExportfilePrefix();
		KindOfExport exportType = reportPanel.getKindOfExport();
		String extension = "." + exportType.toString().toLowerCase(Locale.ROOT);
		String filePathPrefix = exportDirectory + File.separator + prefix;

		Configed.getSavedStates().setProperty(getExportPrefixKey(), prefix);

		configurePanel(exportType);

		Set<String> clients = getClients();

		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("AbstractMultiClientExporter.export.loading.message"));
		ConfigedMain.getMainFrame().activateLoadingCursor();

		int failedCount = exportClients(filePathPrefix, extension, clients);

		ConfigedMain.getMainFrame().deactivateLoadingPane();
		ConfigedMain.getMainFrame().deactivateLoadingCursor();

		showResultMessage(failedCount, clients);
	}

	private void configurePanel(KindOfExport exportType) {
		panelInfo.setAskForOverwrite(!reportPanel.allowOverwriting());
		panelInfo.setKindOfExport(exportType);
		applyExtraSettings();
	}

	private int exportClients(String filePathPrefix, String extension, Set<String> clients) {
		int failedCount = 0;

		for (String client : clients) {
			updatePanelForClient(client);

			String scanDate = formatScanDate(getScanDateForClient(client));
			String filePath = buildFilePath(filePathPrefix, client, scanDate, extension);

			Logging.debug(this, "Exporting to ", filePath);

			panelInfo.setWriteToFile(filePath);

			boolean exported = panelInfo.getSingleClientExporter().export();
			if (!exported) {
				failedCount++;
			}
		}

		return failedCount;
	}

	private static String formatScanDate(String scanDate) {
		if (scanDate == null) {
			return "__";
		}

		int spaceIndex = scanDate.indexOf(' ');
		return (spaceIndex >= 0) ? scanDate.substring(0, spaceIndex) : scanDate;
	}

	private static String buildFilePath(String prefix, String client, String scanDate, String extension) {
		return prefix + client + "__scan_" + scanDate + extension;
	}

	private static void showMissingDirectoryMessage() {
		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.message"),
				Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void showResultMessage(int failedCount, Set<String> clients) {
		StringBuilder msgBuilder = new StringBuilder();
		int msgType = JOptionPane.INFORMATION_MESSAGE;

		if (failedCount > 0) {
			msgBuilder
					.append(Configed.getResourceValue("AbstractMultiClientExporter.exportFinished.withIssues.message"));
			msgBuilder.append("\n");
			msgBuilder.append(
					String.format(Configed.getResourceValue("AbstractMultiClientExporter.export.failedClients.message"),
							failedCount));

			msgType = JOptionPane.WARNING_MESSAGE;
		} else {
			msgBuilder.append(Configed.getResourceValue("AbstractMultiClientExporter.exportFinished.message"));
		}

		int skippedClients = configedMain.getSelectedClients().size() - clients.size();
		if (skippedClients != 0) {
			msgBuilder.append("\n");
			msgBuilder.append(String.format(
					Configed.getResourceValue("AbstractMultiClientExporter.export.skippedClients.message"),
					skippedClients));
		}

		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), msgBuilder.toString(),
				Configed.getResourceValue("AbstractMultiClientExporter.exportFinished.title"), msgType);
	}

	protected abstract String getExportPrefixKey();

	protected abstract void applyExtraSettings();

	protected abstract void updatePanelForClient(String client);

	protected abstract String getScanDateForClient(String client);

	protected abstract Set<String> getClients();
}
