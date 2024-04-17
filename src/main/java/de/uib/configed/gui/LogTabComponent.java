/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.logpane.LogPane;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class LogTabComponent extends LogPane {
	private static final String ALL_LOGFILES_SUFFIX = "all";
	private static final byte[] CRLF = new byte[] { '\r', '\n' };

	private JFileChooser chooser;
	private ConfigedMain configedMain;
	private String logFileType;

	public LogTabComponent(String defaultText, boolean withPopup, ConfigedMain configedMain) {
		super(defaultText, withPopup);
		this.configedMain = configedMain;
	}

	public void setLogFileType(String logFileType) {
		this.logFileType = logFileType;
	}

	@Override
	public void reload() {
		super.reload();
		ConfigedMain.getMainFrame().activateLoadingCursor();
		loadDocument(logFileType);
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	public void loadDocument(String ident) {
		Logging.debug(this, "loadDocument ident " + ident);
	}

	@Override
	public void save() {
		ConfigedMain.getMainFrame().activateLoadingCursor();
		String fileName = retrieveFileName(getInfo(), logFileType);
		String filePath = retrieveFilePath(fileName + ".log");
		if (filePath != null && !filePath.isEmpty()) {
			saveToFile(filePath, getLines());
		}
		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	@Override
	protected void saveAsZip() {
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
	protected void saveAllAsZip() {
		if (configedMain.getSelectedClients() == null || configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		Logging.info(this, "got log file type " + logFileType);

		ConfigedMain.getMainFrame().activateLoadingCursor();
		String fileName = retrieveFileName(getInfo(), ALL_LOGFILES_SUFFIX);
		Logging.info(this, "retrieving file path");
		String filePath = retrieveFilePath(fileName + ".zip");

		if (filePath == null || filePath.isEmpty()) {
			return;
		}

		Logging.info(this, "file path retrieved: " + filePath);

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
			setFileChooser("");
		}

		File f = new File(chooser.getCurrentDirectory(), typeName);
		chooser.setSelectedFile(f);

		int returnVal = chooser.showSaveDialog(ConfigedMain.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}

		return fileName;
	}

	private void setFileChooser(String fileName) {
		chooser = new JFileChooser(fileName);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z", "log", "zip", "gz", "7z"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
	}

	private Map<String, String> retrieveAllLogFiles() {
		Map<String, String> logFiles = new HashMap<>();
		String[] idents = Utils.getLogTypes();
		for (String ident : idents) {
			Map<String, String> logFile = PersistenceControllerFactory.getPersistenceController().getLogDataService()
					.getLogfile(configedMain.getSelectedClients().get(0), ident);
			if (logFile.get(ident) != null && logFile.get(ident).split("\n").length > 1) {
				logFiles.put(ident, logFile.get(ident));
			}

			Logging.info(this, "saveAllAsZip " + ident + " " + logFile.get(ident).split("\n").length);
		}
		return logFiles;
	}

	private static void saveToFile(String fileName, String[] lines) {
		try (FileWriter fWriter = new FileWriter(fileName, StandardCharsets.UTF_8)) {
			for (int i = 0; i < lines.length; i++) {
				fWriter.write(lines[i] + "\n");
			}
		} catch (IOException ex) {
			Logging.error("Error writing to a file: " + fileName + "\n --- " + ex);
		}
	}

	private void saveAllToZipFile(String filePath, Map<String, String> logFiles) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			String[] idents = Utils.getLogTypes();
			for (String ident : idents) {
				if (logFiles.get(ident) != null && logFiles.get(ident).split("\n").length > 1) {
					String fileName = retrieveFileName(configedMain.getSelectedClients().get(0).replace(".", "_"),
							ident);
					ZipEntry entry = new ZipEntry(fileName + ".log");
					out.putNextEntry(entry);
					writeToOutputStream(logFiles.get(ident).split("\n"), out);
				}
			}
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + filePath + "\n --- " + ex);
		}
	}

	private static void saveToZipFile(String filePath, String fileName, String[] lines) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry entry = new ZipEntry(fileName);
			out.putNextEntry(entry);
			writeToOutputStream(lines, out);
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + fileName + "\n --- " + ex);
		}
	}

	private static void writeToOutputStream(String[] lines, ZipOutputStream out) {
		for (int i = 0; i < lines.length; i++) {
			try {
				byte[] buffer = lines[i].getBytes(StandardCharsets.UTF_8);
				out.write(buffer, 0, lines[i].length());
				out.write(CRLF, 0, 2);
			} catch (IOException ex) {
				Logging.error("Error writing zip file: " + "\n --- " + ex);
			}
		}
	}
}
