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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.logpane.LogPane;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ClippedTitleTabbedPane;

public class PanelTabbedDocuments extends ClippedTitleTabbedPane {
	private static final byte[] CRLF = new byte[] { '\r', '\n' };

	private LogPane[] textPanes;
	private String[] idents;
	private final List<String> identsList;

	private JFileChooser chooser;
	private File chooserDirectory;
	private ConfigedMain configedMain;

	public PanelTabbedDocuments(final String[] idents, String defaultText, ConfigedMain configedMain) {
		this.idents = idents;
		this.configedMain = configedMain;

		identsList = Arrays.asList(idents);

		super.setProportionOfTotalWidth(0.5);

		textPanes = new LogPane[idents.length];

		for (int i = 0; i < idents.length; i++) {
			initLogFrame(i, defaultText);
		}

		super.setBorder(new EmptyBorder(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
				Globals.MIN_GAP_SIZE));
	}

	private void initLogFrame(int i, String defaultText) {
		LogPane showPane = new LogPane(defaultText, true) {
			@Override
			public void reload() {
				super.reload();
				loadDocument(idents[i]);
			}

			@Override
			public void save() {
				String filename = idents[i];
				if (getInfo() != null) {
					filename = getInfo().replace('.', '_') + "___" + idents[i] + ".log";
				}
				Logging.debug(this, "save with filename " + filename);
				String pathname = openFile(filename + ".log");
				if (pathname != null && !pathname.isEmpty()) {
					saveToFile(pathname, getLines());
				}
			}

			@Override
			protected void saveAsZip() {
				Logging.info(this, "saveAsZip");

				String filename = idents[i];
				if (getInfo() != null) {
					filename = getInfo().replace('.', '_') + "___" + idents[i] + ".log";
				}
				String pathname = openFile(filename + ".zip");
				if (pathname != null && !pathname.isEmpty()) {
					saveToZipFile(pathname, filename, getLines());
				}

			}

			@Override
			protected void saveAllAsZip(boolean loadMissingDocs) {
				if (configedMain.getSelectedClients() == null || configedMain.getSelectedClients().length <= 0) {
					return;
				}

				Logging.info(this, "saveAllAsZip got ident " + idents[i] + " loadMissingDocs " + loadMissingDocs);

				String fname = idents[i];
				if (getInfo() != null) {
					fname = getInfo().replace('.', '_') + "_all";
				}

				Logging.info(this, "saveAllAsZip, start getting pathname");
				String pathname = openFile(fname + ".zip");

				if (pathname == null || pathname.isEmpty()) {
					return;
				}

				Logging.info(this, "saveAllAsZip, got pathname");

				Map<String, String> logfiles = new HashMap<>();
				if (loadMissingDocs) {
					for (int logNo = 0; logNo < idents.length; logNo++) {
						Map<String, String> logfile = PersistenceControllerFactory.getPersistenceController()
								.getLogDataService().getLogfile(configedMain.getSelectedClients()[0], idents[logNo]);
						if (logfile.get(idents[logNo]).split("\n").length > 1) {
							logfiles.put(idents[logNo], logfile.get(idents[logNo]));
						}

						Logging.info(this,
								"saveAllAsZip " + idents[logNo] + " " + logfile.get(idents[logNo]).split("\n").length);
					}
				}
				saveAllToZipFile(pathname, logfiles);
			}
		};

		textPanes[i] = showPane;

		super.addTab(idents[i], textPanes[i]);
	}

	// override in subclasses
	public void loadDocument(String ident) {

		Logging.debug(this, "loadDocument ident " + ident);
	}

	private void setDocument(int i, final String document, final String info) {
		Logging.info(this, "setDocument " + i + " document == null " + (document == null));
		if (i < 0 || i >= idents.length) {
			return;
		}

		if (document == null) {
			textPanes[i].setText("");
			textPanes[i].setTitle("");
			return;
		}

		textPanes[i].setTitle(idents[i] + "  " + info);

		// should be name of client, delivered from info textfield
		textPanes[i].setInfo(info);
		textPanes[i].setText(document);
	}

	private void setDocument(String ident, final String document, final String info) {
		int i = identsList.indexOf(ident);
		setDocument(i, document, info);
	}

	public void setDocuments(final Map<String, String> documents, final String info) {
		Logging.info(this, "idents.length " + idents.length + " info: " + info);
		for (String ident : idents) {
			setDocument(ident, documents.get(ident), info);
		}

	}

	private void setFileChooser(String fn) {
		if (chooser == null) {
			chooser = new JFileChooser(fn);
			chooser.setPreferredSize(Globals.FILE_CHOOSER_SIZE);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(
					new FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z", "log", "zip", "gz", "7z"));
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(
					Globals.APPNAME + " " + Configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
		}
	}

	private String openFile(String typename) {
		String fileName = null;

		// Guarantee that chooser is not null
		if (chooser == null) {
			setFileChooser("");
			chooserDirectory = chooser.getCurrentDirectory();
		}

		File f = new File(chooserDirectory, typename);
		chooser.setSelectedFile(f);

		int returnVal = chooser.showSaveDialog(ConfigedMain.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
			chooserDirectory = chooser.getCurrentDirectory();
		}

		return fileName;
	}

	private static void saveToFile(String fn, String[] lines) {
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(fn, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			Logging.error("Error opening file: " + fn + "\n --- " + ex);
			return;
		}
		int i = 0;
		while (i < lines.length) {
			try {
				fWriter.write(lines[i] + "\n");

			} catch (IOException ex) {
				Logging.error("Error writing file: " + fn + "\n --- " + ex);
			}
			i++;
		}
		try {
			fWriter.close();
		} catch (IOException ex) {
			Logging.error("Error closing file: " + fn + "\n --- " + ex);
		}
	}

	private void saveAllToZipFile(String pn, Map<String, String> logfiles) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pn))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			for (int logNo = 0; logNo < idents.length; logNo++) {
				if (logfiles.get(idents[logNo]) != null && logfiles.get(idents[logNo]).split("\n").length > 1) {
					ZipEntry entry = new ZipEntry(
							configedMain.getSelectedClients()[0].replace(".", "_") + "___" + idents[logNo] + ".log");
					out.putNextEntry(entry);
					writeToOutputStream(logfiles.get(idents[logNo]).split("\n"), out);
				}
				out.closeEntry();
			}
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + pn + "\n --- " + ex);
		}
	}

	private static void saveToZipFile(String pn, String fn, String[] lines) {

		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pn))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry entry = new ZipEntry(fn);
			out.putNextEntry(entry);

			writeToOutputStream(lines, out);

			out.closeEntry();
		} catch (IOException ex) {
			Logging.error("Error writing zip file: " + fn + "\n --- " + ex);
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
