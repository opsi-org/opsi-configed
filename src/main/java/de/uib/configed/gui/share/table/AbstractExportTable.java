/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.awt.Container;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public abstract class AbstractExportTable {
	protected JTable theTable;

	protected Map<String, String> metaData;

	protected FileNameExtensionFilter extensionFilter;

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
		Icons.addIntellijIconToMenuItem(menuItem, "export");
		menuItem.addActionListener(actionEvent -> execute(null, false));
		return menuItem;
	}

	public void addMenuItemsTo(Container component) {
		component.add(getMenuItemExport());
		component.add(getMenuItemExportSelected());
	}

	public JMenuItem getMenuItemExportSelected() {
		JMenuItem menuItem = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV"));
		Icons.addIntellijIconToMenuItem(menuItem, "export");
		menuItem.addActionListener(actionEvent -> execute(null, true));

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
			Logging.debug("selectedRows: ", theTable.getSelectedRowCount());
			if (theTable.getRowCount() > 0 && theTable.getSelectedRowCount() == 0) {
				String message = Configed.getResourceValue("ExportTable.caseNoSelectedRows.info") + "\n\n\n"
						+ Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly.text")
						+ "\n\n"
						+ Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCompleteTable.text");

				int answer = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(), message,
						Configed.getResourceValue("ExportTable.title"), JOptionPane.OK_OPTION,
						JOptionPane.PLAIN_MESSAGE, null,
						new Object[] {
								Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly"),
								Configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCompleteTable"),
								Configed.getResourceValue("buttonCancel") },
						null);

				Logging.info(this, "checkSelection answered ", answer);
				if (answer == 0) {
					result = true;
				} else if (answer == 1) {
					result = false;
				} else {
					result = null;
				}
			}
		}
		Logging.info(this, "checkSelection gives: onlySelectedRows = ", result);

		return result;
	}

	protected String checkFile(String filename, FileNameExtensionFilter exFilter) {
		if (filename == null) {
			SystemFileChooser fileChooser = new SystemFileChooser(exportDirectory);
			fileChooser.setFileHidingEnabled(false);
			fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);

			fileChooser.addChoosableFileFilter(exFilter);

			fileChooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
			fileChooser.setDialogTitle(Configed.getResourceValue("DocumentExport.chooser"));

			fileChooser.setApproveButtonText(Configed.getResourceValue("buttonOK"));

			int returnVal = fileChooser.showDialog(ConfigedMain.getMainFrame(), null);
			if (returnVal == SystemFileChooser.APPROVE_OPTION) {
				filename = fileChooser.getSelectedFile().getAbsolutePath();

				if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
					filename = filename + ".csv";
				}

				Logging.debug(this, "filename ", filename);

				if (askForOverwrite) {
					filename = askForOverride(filename);
				}
			}
		} else {
			exportDirectory = new File(filename).getParentFile();
		}

		Logging.debug(this, "export to ", filename);

		return filename;
	}

	private static String askForOverride(String filename) {
		try {
			File file = new File(filename);
			if (file.exists()) {
				int option = JOptionPane.showConfirmDialog(Utils.getMasterFrame(),
						Configed.getResourceValue("DocumentExport.showConfirmDialog") + "\n" + file.getName(),
						Configed.getResourceValue("DocumentExport.fileAlreadyExists"), JOptionPane.OK_CANCEL_OPTION);

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

		Logging.info(this, "getFileLocation with writeToFile ", writeToFile);

		File defaultFile = new File(writeToFile);

		SystemFileChooser fileChooser = new SystemFileChooser(exportDirectory);
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));

		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle(Configed.getResourceValue("DocumentExport.chooser"));
		int returnVal = fileChooser.showDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("buttonOK"));
		if (returnVal == SystemFileChooser.APPROVE_OPTION) {
			fileName = fileChooser.getSelectedFile().getAbsolutePath();
			Logging.info(this, "clicked ok on JFileChosser, get now fileName: ", fileName);
		}

		if (fileName != null) {
			exportDirectory = new File(fileName).getParentFile();
		}

		return fileName;
	}
}
