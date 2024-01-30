/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.logpane.LogPane;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class PanelTabbedDocuments extends JTabbedPane {
	private static final String ALL_LOGFILES_SUFFIX = "all";
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

		textPanes = new LogPane[idents.length];

		for (int i = 0; i < idents.length; i++) {
			initLogFrame(i, defaultText);
		}

		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				initTabWidth();
			}
		});
		super.addChangeListener(changeEvent -> initTabWidth());
	}

	private void initTabWidth() {
		Insets tabInsets = getTabInsets();
		Insets tabAreaInsets = getTabAreaInsets();
		Insets insets = getInsets();
		int areaWidth = calcWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
		int tabCount = getTabCount();
		int tabWidth = 0;
		int gap = 0;
		switch (getTabPlacement()) {
		case LEFT:
		case RIGHT:
			tabWidth = areaWidth / 4;
			gap = 0;
			break;
		case BOTTOM:
		case TOP:
		default:
			tabWidth = areaWidth / tabCount;
			gap = areaWidth - (tabWidth * tabCount);
			break;
		}

		tabWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
		for (int i = 0; i < tabCount; i++) {
			Component tabComponent = getTabComponentAt(i);
			if (tabComponent == null) {
				break;
			}

			if (i < gap) {
				tabWidth = tabWidth + 1;
			}
			tabComponent.setPreferredSize(new Dimension(tabWidth, tabComponent.getPreferredSize().height));
		}
		revalidate();
	}

	private Insets getTabInsets() {
		return getInsets("TabbedPane.tabInsets", Region.TABBED_PANE_TAB);
	}

	private Insets getTabAreaInsets() {
		return getInsets("TabbedPane.tabAreaInsets", Region.TABBED_PANE_TAB_AREA);
	}

	private Insets getInsets(String insetsKey, Region insetsRegion) {
		Insets insets = UIManager.getInsets(insetsKey);
		if (insets == null) {
			SynthStyle style = SynthLookAndFeel.getStyle(this, insetsRegion);
			SynthContext context = new SynthContext(this, insetsRegion, style, SynthConstants.ENABLED);
			insets = style.getInsets(context, null);
		}
		return insets;
	}

	private int calcWidth() {
		double proportionOfTotalWidth = 0.5;
		return (int) (getWidth() * proportionOfTotalWidth);
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		Dimension dim = label.getPreferredSize();
		Insets tabInsets = getTabInsets();
		label.setPreferredSize(new Dimension(0, dim.height + tabInsets.top + tabInsets.bottom));
		setTabComponentAt(index, label);
		initTabWidth();
	}

	private void initLogFrame(int i, String defaultText) {
		LogPane showPane = new LogPane(defaultText, true) {
			@Override
			public void reload() {
				super.reload();
				ConfigedMain.getMainFrame().activateLoadingCursor();
				loadDocument(idents[i]);
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			}

			@Override
			public void save() {
				ConfigedMain.getMainFrame().activateLoadingCursor();
				String fileName = retrieveFileName(getInfo(), idents[i]);
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
				String fileName = retrieveFileName(getInfo(), idents[i]);
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

				Logging.info(this, "got ident " + idents[i]);

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
		};

		textPanes[i] = showPane;

		super.addTab(idents[i], textPanes[i]);
	}

	private static String retrieveFileName(String clientName, String suffix) {
		String fileName = suffix;
		if (clientName != null) {
			suffix = ALL_LOGFILES_SUFFIX.equals(suffix) ? ALL_LOGFILES_SUFFIX : (suffix + ".log");
			fileName = clientName.replace('.', '_') + "___" + suffix;
		}
		return fileName;
	}

	private Map<String, String> retrieveAllLogFiles() {
		Map<String, String> logfiles = new HashMap<>();
		for (String ident : idents) {
			Map<String, String> logfile = PersistenceControllerFactory.getPersistenceController().getLogDataService()
					.getLogfile(configedMain.getSelectedClients().get(0), ident);
			if (logfile.get(ident).split("\n").length > 1) {
				logfiles.put(ident, logfile.get(ident));
			}

			Logging.info(this, "saveAllAsZip " + ident + " " + logfile.get(ident).split("\n").length);
		}
		return logfiles;
	}

	public void loadDocument(String ident) {
		Logging.debug(this, "loadDocument ident " + ident);
	}

	private String retrieveFilePath(String typeName) {
		String fileName = null;

		if (chooser == null) {
			setFileChooser("");
			chooserDirectory = chooser.getCurrentDirectory();
		}

		File f = new File(chooserDirectory, typeName);
		chooser.setSelectedFile(f);

		int returnVal = chooser.showSaveDialog(ConfigedMain.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
			chooserDirectory = chooser.getCurrentDirectory();
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

	private static void saveToFile(String fileName, String[] lines) {
		try (FileWriter fWriter = new FileWriter(fileName, StandardCharsets.UTF_8)) {
			for (int i = 0; i < lines.length; i++) {
				fWriter.write(lines[i] + "\n");
			}
		} catch (IOException ex) {
			Logging.error("Error writing to a file: " + fileName + "\n --- " + ex);
		}
	}

	private void saveAllToZipFile(String filePath, Map<String, String> logfiles) {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath))) {
			out.setMethod(ZipOutputStream.DEFLATED);
			for (String ident : idents) {
				if (logfiles.get(ident) != null && logfiles.get(ident).split("\n").length > 1) {
					String fileName = retrieveFileName(configedMain.getSelectedClients().get(0).replace(".", "_"),
							ident);
					ZipEntry entry = new ZipEntry(fileName);
					out.putNextEntry(entry);
					writeToOutputStream(logfiles.get(ident).split("\n"), out);
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

	public void setDocuments(final Map<String, String> documents, final String info) {
		Logging.info(this, "idents.length " + idents.length + " info: " + info);
		for (String ident : idents) {
			setDocument(ident, documents.get(ident), info);
		}
	}

	private void setDocument(String ident, final String document, final String info) {
		int i = identsList.indexOf(ident);
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
		textPanes[i].setInfo(info);
		textPanes[i].setText(document);
	}
}
