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
	private static final byte[] CRLF = new byte[] { '\r', '\n' };

	private LogPane[] textPanes;
	private String[] idents;
	private final List<String> identsList;

	private JFileChooser chooser;
	private File chooserDirectory;
	private ConfigedMain configedMain;

	private double proportionOfTotalWidth;

	public PanelTabbedDocuments(final String[] idents, String defaultText, ConfigedMain configedMain) {
		this.idents = idents;
		this.configedMain = configedMain;

		identsList = Arrays.asList(idents);

		proportionOfTotalWidth = 0.5;

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
			JLabel l = (JLabel) getTabComponentAt(i);
			if (l == null) {
				break;
			}

			if (i < gap) {
				tabWidth = tabWidth + 1;
			}
			l.setPreferredSize(new Dimension(tabWidth, l.getPreferredSize().height));
		}
		revalidate();
	}

	private int calcWidth() {
		return (int) (getWidth() * proportionOfTotalWidth);
	}

	private Insets getTabInsets() {
		Insets i = UIManager.getInsets("TabbedPane.tabInsets");
		if (i != null) {
			return i;
		} else {
			SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
			SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
			return style.getInsets(context, null);
		}
	}

	private Insets getTabAreaInsets() {
		Insets i = UIManager.getInsets("TabbedPane.tabAreaInsets");
		if (i != null) {
			return i;
		} else {
			SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
			SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
			return style.getInsets(context, null);
		}
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
				String filename = idents[i];
				if (getInfo() != null) {
					filename = getInfo().replace('.', '_') + "___" + idents[i] + ".log";
				}
				Logging.debug(this, "save with filename " + filename);
				String pathname = openFile(filename + ".log");
				if (pathname != null && !pathname.isEmpty()) {
					saveToFile(pathname, getLines());
				}
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			}

			@Override
			protected void saveAsZip() {
				Logging.info(this, "saveAsZip");

				ConfigedMain.getMainFrame().activateLoadingCursor();
				String filename = idents[i];
				if (getInfo() != null) {
					filename = getInfo().replace('.', '_') + "___" + idents[i] + ".log";
				}
				String pathname = openFile(filename + ".zip");
				if (pathname != null && !pathname.isEmpty()) {
					saveToZipFile(pathname, filename, getLines());
				}
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
			}

			@Override
			protected void saveAllAsZip(boolean loadMissingDocs) {
				if (configedMain.getSelectedClients() == null || configedMain.getSelectedClients().isEmpty()) {
					return;
				}

				Logging.info(this, "saveAllAsZip got ident " + idents[i] + " loadMissingDocs " + loadMissingDocs);

				ConfigedMain.getMainFrame().activateLoadingCursor();
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
					for (String ident : idents) {
						Map<String, String> logfile = PersistenceControllerFactory.getPersistenceController()
								.getLogDataService().getLogfile(configedMain.getSelectedClients().get(0), ident);
						if (logfile.get(ident).split("\n").length > 1) {
							logfiles.put(ident, logfile.get(ident));
						}

						Logging.info(this, "saveAllAsZip " + ident + " " + logfile.get(ident).split("\n").length);
					}
				}
				saveAllToZipFile(pathname, logfiles);
				ConfigedMain.getMainFrame().deactivateLoadingCursor();
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
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(
					new FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z", "log", "zip", "gz", "7z"));
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(Configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
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
			for (String ident : idents) {
				if (logfiles.get(ident) != null && logfiles.get(ident).split("\n").length > 1) {
					ZipEntry entry = new ZipEntry(
							configedMain.getSelectedClients().get(0).replace(".", "_") + "___" + ident + ".log");
					out.putNextEntry(entry);
					writeToOutputStream(logfiles.get(ident).split("\n"), out);
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
