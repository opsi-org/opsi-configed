/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public abstract class AbstractExportTable {
	protected JTable theTable;

	protected Map<String, String> metaData;

	protected FileNameExtensionFilter extensionFilter;
	protected String defaultExportFilename;

	private File exportDirectory;

	private boolean askForOverwrite;

	protected String writeToFile;

	protected String client;

	protected String extension;

	protected AbstractExportTable(JTable table) {
		this.theTable = table;
		askForOverwrite = true;
	}

	public void setAskForOverwrite(boolean b) {
		askForOverwrite = b;
	}

	protected abstract void execute(String fileName, boolean onlySelectedRows);

	public JMenuItem getMenuItemExport() {
		JMenuItem menuItem = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportTableAsCSV"));
		Utils.addIntellijIconToMenuItem(menuItem, "export");
		menuItem.addActionListener(actionEvent -> execute(null, false));
		return menuItem;
	}

	public void addMenuItemsTo(Container component) {
		component.add(getMenuItemExport());
		component.add(getMenuItemExportSelected());
	}

	public JMenuItem getMenuItemExportSelected() {
		JMenuItem menuItem = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV"));
		Utils.addIntellijIconToMenuItem(menuItem, "export");
		menuItem.addActionListener((ActionEvent actionEvent) -> execute(null, true));

		return menuItem;
	}

	public void setClient(String clientID) {
		client = clientID;
	}

	public void setMetaData(Map<String, String> tableMetaData) {
		metaData = tableMetaData;
	}

	protected String checkExtension(String path) {
		if (path == null) {
			return null;
		}

		if (!path.toLowerCase(Locale.ROOT).endsWith(extension)) {
			path = path + extension;
		}

		return path;
	}

	protected Boolean checkSelection(boolean onlySelectedRows) {
		Boolean result = onlySelectedRows;

		if (onlySelectedRows) {
			Logging.debug("selectedRows: " + theTable.getSelectedRowCount());
			if (theTable.getRowCount() > 0 && theTable.getSelectedRowCount() == 0) {
				FTextArea fChoice = new FTextArea(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("ExportTable.title"), true,
						new String[] { Configed.getResourceValue("buttonCancel"),
								Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly"),
								Configed.getResourceValue(
										"ExportTable.caseNoSelectedRows.option.exportCompleteTable") },
						500, 200);
				fChoice.setDefaultResult(3);
				fChoice.setMessage(Configed.getResourceValue("ExportTable.caseNoSelectedRows.info") + "\n\n\n"
						+ Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly.text")
						+ "\n\n"
						+ Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCompleteTable.text")
						+ "\n\n");
				fChoice.setVisible(true);

				int answer = fChoice.getResult();

				result = null;
				Logging.info(this, "checkSelection answered " + answer);
				if (answer == 1) {
					result = true;
				} else if (answer == 2) {
					result = false;
				} else {
					Logging.warning(this, "unexpected answer " + answer);
				}
			}
		}
		Logging.info(this, "checkSelection gives: onlySelectedRows = " + result);

		return result;
	}

	protected String checkFile(String filename, FileNameExtensionFilter exFilter) {
		if (filename == null) {
			JFileChooser chooser = new JFileChooser(exportDirectory);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			chooser.addChoosableFileFilter(exFilter);

			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(Configed.getResourceValue("DocumentExport.chooser"));

			chooser.setApproveButtonText(Configed.getResourceValue("buttonOK"));
			chooser.setApproveButtonToolTipText(Configed.getResourceValue("ExportTable.approveTooltip"));

			SwingUtilities.updateComponentTreeUI(chooser);

			int returnVal = chooser.showDialog(ConfigedMain.getMainFrame(), null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getAbsolutePath();

				File file = new File(filename);

				if (file.isDirectory()) {
					filename = filename + File.separator + defaultExportFilename;
				} else if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
					filename = filename + ".csv";
				} else {
					// Do nothing when it's a file with ending ".csv"
				}

				Logging.debug(this, "filename " + filename);

				if (askForOverwrite) {
					filename = askForOverride(filename);
				}
			}
		} else {
			exportDirectory = new File(filename).getParentFile();
		}

		Logging.debug(this, "export to " + filename);

		return filename;
	}

	private static String askForOverride(String filename) {
		try {
			File file = new File(filename);
			if (file.exists()) {
				int option = JOptionPane.showConfirmDialog(Utils.getMasterFrame(),
						Configed.getResourceValue("DocumentExport.showConfirmDialog") + "\n" + file.getName(),
						Configed.getResourceValue("DocumentExport.question"), JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.CANCEL_OPTION) {
					return null;
				}
			}
		} catch (HeadlessException exception) {
			Logging.error(exception, Configed.getResourceValue("DocumentExport.errorNoValidFilename"), "\n", filename);
		}

		return filename;
	}

	protected String getFileLocation() {
		String fileName = null;

		Logging.info(this, "getFileLocation with writeToFile " + writeToFile);

		File defaultFile = new File(writeToFile);

		JFileChooser chooser = new JFileChooser(exportDirectory);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));

		chooser.setSelectedFile(defaultFile);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Configed.getResourceValue("DocumentExport.chooser"));

		int returnVal = chooser.showDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("buttonOK"));
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
			Logging.info(this, "clicked ok on JFileChosser, get now fileName: " + fileName);
		}

		if (fileName != null) {
			exportDirectory = new File(fileName).getParentFile();
		}

		return fileName;
	}
}
