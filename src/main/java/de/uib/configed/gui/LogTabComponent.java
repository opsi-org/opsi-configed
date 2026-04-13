/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.util.SystemFileChooser;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneComponent;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneModel;
import de.uib.configed.share.logging.Logging;

public class LogTabComponent extends LogPaneComponent {
	protected static final String[] LOG_TYPES = new String[] { "bootimage", "clientconnect", "instlog", "opsiconfd",
			"userlogin" };
	private static final String ALL_LOGFILES_SUFFIX = "all";
	private static final String LOGFILE_EXTENSION = ".log";
	private static final byte[] CRLF = new byte[] { '\r', '\n' };

	private SystemFileChooser chooser;
	private ConfigedMain configedMain;
	private String logFileType;

	public LogTabComponent(String defaultText, boolean withPopup, ConfigedMain configedMain) {
		super(LogPaneModel.builder().logText(defaultText).withPopup(withPopup).build());
		this.configedMain = configedMain;
	}

	public void setLogFileType(String logFileType) {
		this.logFileType = logFileType;
	}

	@Override
	public void reload() {
		super.reload();
		Logging.info("reload logFileType ", logFileType);

		ConfigedMain.getMainFrame().activateLoadingCursor();
		Rectangle visibleRectangle = logTextPane.getVisibleRect();
		int caretPosition = logTextPane.getCaretPosition();
		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().setLogFileTab(logFileType, true);
		SwingUtilities.invokeLater(() -> {
			logTextPane.setCaretPosition(caretPosition);
			logTextPane.scrollRectToVisible(visibleRectangle);
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		});
	}

	@Override
	public void download() {
		ConfigedMain.getMainFrame().activateLoadingCursor();
		String fileName = retrieveFileName(getInfo(), logFileType);
		String filePath = retrieveFilePath(fileName + LOGFILE_EXTENSION);
		if (filePath != null && !filePath.isEmpty()) {
			saveToFile(filePath, getLines());
		}
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	@Override
	protected void downloadAsZip() {
		Logging.info(this, "saveAsZip");
		ConfigedMain.getMainFrame().activateLoadingCursor();
		String fileName = retrieveFileName(getInfo(), logFileType);
		String filePath = retrieveFilePath(fileName + ".zip");
		if (filePath != null && !filePath.isEmpty()) {
			saveToZipFile(filePath, fileName, getLines());
		}
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	@Override
	protected void downloadAllAsZip() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		Logging.info(this, "got log file type ", logFileType);

		ConfigedMain.getMainFrame().activateLoadingCursor();
		String fileName = retrieveFileName(getInfo(), ALL_LOGFILES_SUFFIX);
		Logging.info(this, "retrieving file path");
		String filePath = retrieveFilePath(fileName + ".zip");

		if (filePath == null || filePath.isEmpty()) {
			return;
		}

		Logging.info(this, "file path retrieved: ", filePath);

		saveAllToZipFile(filePath, retrieveAllLogFiles());
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private static String retrieveFileName(String clientName, String suffix) {
		String fileName = suffix;
		if (clientName != null) {
			fileName = clientName.replace('.', '_') + "___" + suffix;
		}
		return fileName;
	}

	private String retrieveFilePath(String typeName) {
		String fileName = null;

		if (chooser == null) {
			setFileChooser();
		}

		File f = new File(chooser.getCurrentDirectory(), typeName);
		chooser.setSelectedFile(f);

		int returnVal = chooser.showSaveDialog(ConfigedMain.getMainFrame());
		if (returnVal == SystemFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}

		return fileName;
	}

	private void setFileChooser() {
		chooser = new SystemFileChooser();
		chooser.setFileHidingEnabled(false);
		chooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		chooser.setFileFilter(new SystemFileChooser.FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z", "log",
				"zip", "gz", "7z"));
		chooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
	}

	private Map<String, String> retrieveAllLogFiles() {
		Map<String, String> logFiles = new HashMap<>();
		for (String ident : LOG_TYPES) {
			String logfile = PersistenceControllerFactory.getPersistenceController().getDataServices().log
					.getLogfile(configedMain.getSelectedClients().get(0), ident);
			if (logfile.split("\n").length > 1) {
				logFiles.put(ident, logfile);
			}

			Logging.info(this, "saveAllAsZip ", ident, " ", logfile.split("\n").length);
		}
		return logFiles;
	}

	private static void saveToFile(String fileName, String[] lines) {
		try (FileWriter fWriter = new FileWriter(fileName, StandardCharsets.UTF_8)) {
			for (int i = 0; i < lines.length; i++) {
				fWriter.write(lines[i] + "\n");
			}
		} catch (IOException ex) {
			Logging.error(ex, "Error writing to a file: ", fileName);
		}
	}

	private void saveAllToZipFile(String filePath, Map<String, String> logFiles) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			for (String ident : LOG_TYPES) {
				if (logFiles.get(ident) != null && logFiles.get(ident).split("\n").length > 1) {
					String fileName = retrieveFileName(configedMain.getSelectedClients().get(0).replace(".", "_"),
							ident);
					ZipEntry entry = new ZipEntry(fileName + LOGFILE_EXTENSION);
					out.putNextEntry(entry);
					writeToOutputStream(logFiles.get(ident).split("\n"), out);
				}
			}
		} catch (IOException ex) {
			Logging.error(ex, "Error writing zip file: ", filePath);
		}
	}

	private static void saveToZipFile(String filePath, String fileName, String[] lines) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry entry = new ZipEntry(fileName + LOGFILE_EXTENSION);
			out.putNextEntry(entry);
			writeToOutputStream(lines, out);
		} catch (IOException ex) {
			Logging.error(ex, "Error writing zip file: ", fileName);
		}
	}

	private static void writeToOutputStream(String[] lines, ZipOutputStream out) {
		for (int i = 0; i < lines.length; i++) {
			try {
				byte[] buffer = lines[i].getBytes(StandardCharsets.UTF_8);
				out.write(buffer, 0, lines[i].length());
				out.write(CRLF, 0, 2);
			} catch (IOException ex) {
				Logging.error(ex, "Error writing zip file: ");
			}
		}
	}
}
