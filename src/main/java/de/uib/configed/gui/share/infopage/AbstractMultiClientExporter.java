/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.infopage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;
import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
import de.uib.configed.gui.share.table.AbstractExportTable;
import de.uib.configed.gui.share.table.AbstractExportTable.OverwriteDecision;
import lombok.Data;

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
		if (!isValidExportDirectory(exportDirectory)) {
			showMissingDirectoryMessage();
			return;
		}

		ExportConfig config = buildExportConfig(exportDirectory);

		OverwriteDecision decision = determineOverwriteDecision(config);
		if (decision == OverwriteDecision.CANCEL) {
			return;
		}

		int manuallySkipped = decision == OverwriteDecision.SKIP_ALL ? config.existingFiles.size() : 0;

		showLoadingIndicator();

		Set<String> clients = getClients();
		int failedCount = exportClients(config, clients, decision);

		hideLoadingIndicator();

		int totalSkipped = configedMain.getSelectedClients().size() - clients.size();
		totalSkipped += manuallySkipped;
		showResultMessage(failedCount, totalSkipped);
	}

	private static boolean isValidExportDirectory(String dir) {
		return dir != null && !dir.isEmpty();
	}

	private ExportConfig buildExportConfig(String exportDirectory) {
		String prefix = reportPanel.getExportfilePrefix();
		KindOfExport exportType = reportPanel.getKindOfExport();
		String extension = "." + exportType.toString().toLowerCase(Locale.ROOT);
		String filePathPrefix = exportDirectory + File.separator + prefix;

		Configed.getSavedStates().setProperty(getExportPrefixKey(), prefix);
		configurePanel(exportType);

		List<String> allFilePaths = buildAllFilePaths(filePathPrefix, extension);
		Set<String> existingFiles = reportPanel.allowOverwriting() ? Collections.emptySet()
				: AbstractExportTable.checkExistingFiles(allFilePaths);

		return new ExportConfig(filePathPrefix, extension, existingFiles);
	}

	private List<String> buildAllFilePaths(String prefix, String extension) {
		List<String> paths = new ArrayList<>();
		for (String client : getClients()) {
			updatePanelForClient(client);
			String scanDate = formatScanDate(getScanDateForClient(client));
			paths.add(buildFilePath(prefix, client, scanDate, extension));
		}
		return paths;
	}

	private OverwriteDecision determineOverwriteDecision(ExportConfig config) {
		if (reportPanel.allowOverwriting() || config.existingFiles.isEmpty()) {
			return OverwriteDecision.CONTINUE;
		}
		return AbstractExportTable.askForMultipleOverwrites(config.existingFiles);
	}

	private static void showLoadingIndicator() {
		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("AbstractMultiClientExporter.export.loading.message"));
		ConfigedMain.getMainFrame().activateLoadingCursor();
	}

	private static void hideLoadingIndicator() {
		ConfigedMain.getMainFrame().deactivateLoadingPane();
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private int exportClients(ExportConfig config, Set<String> clients, OverwriteDecision decision) {
		int failedCount = 0;

		for (String client : clients) {
			updatePanelForClient(client);
			String scanDate = formatScanDate(getScanDateForClient(client));
			String filePath = buildFilePath(config.filePathPrefix, client, scanDate, config.extension);

			panelInfo.setWriteToFile(filePath);
			panelInfo.setOverwriteDecision(decision);

			boolean exported = panelInfo.getSingleClientExporter().export();
			if (!exported) {
				failedCount++;
			}
		}

		return failedCount;
	}

	private static void showResultMessage(int failedCount, int totalSkipped) {
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

		if (totalSkipped > 0) {
			msgBuilder.append("\n");
			msgBuilder.append(String.format(
					Configed.getResourceValue("AbstractMultiClientExporter.export.skippedClients.message"),
					totalSkipped));
		}

		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), msgBuilder.toString(),
				Configed.getResourceValue("AbstractMultiClientExporter.exportFinished.title"), msgType);
	}

	@Data
	private static class ExportConfig {
		final String filePathPrefix;
		final String extension;
		final Set<String> existingFiles;
	}

	private void configurePanel(KindOfExport exportType) {
		panelInfo.setAskForOverwrite(!reportPanel.allowOverwriting());
		panelInfo.setKindOfExport(exportType);
		applyExtraSettings();
	}

	private static String formatScanDate(String scanDate) {
		if (scanDate == null) {
			return "__";
		}

		int spaceIndex = scanDate.indexOf(' ');
		return (spaceIndex >= 0) ? scanDate.substring(0, spaceIndex) : scanDate;
	}

	private static String buildFilePath(String prefix, String client, String scanDate, String extension) {
		return prefix + client + "_" + scanDate + extension;
	}

	private static void showMissingDirectoryMessage() {
		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.message"),
				Configed.getResourceValue("AbstractMultiClientExporter.missingInformation.title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	protected abstract String getExportPrefixKey();

	protected abstract void applyExtraSettings();

	protected abstract void updatePanelForClient(String client);

	protected abstract String getScanDateForClient(String client);

	protected abstract Set<String> getClients();
}
