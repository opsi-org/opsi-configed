/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.awt.Container;
import java.awt.HeadlessException;
import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public abstract class AbstractExportTable {
	protected JTable theTable;

	protected Map<String, String> metaData;

	protected FileNameExtensionFilter extensionFilter;

	private File exportDirectory;

	private boolean askForOverwrite;

	protected String extension;

	public enum OverwriteDecision {
		OVERWRITE_ALL, SKIP_ALL, CANCEL, CONTINUE
	}

	protected AbstractExportTable(JTable table) {
		this.theTable = table;
		askForOverwrite = true;
	}

	public void setAskForOverwrite(boolean b) {
		askForOverwrite = b;
	}

	protected abstract boolean execute(String fileName, boolean onlySelectedRows);

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

	public static Set<String> checkExistingFiles(Iterable<String> fileNames) {
		Set<String> existingFiles = new HashSet<>();

		for (String fileName : fileNames) {
			if (fileName != null && new File(fileName).exists()) {
				existingFiles.add(fileName);
			}
		}

		return existingFiles;
	}

	public static OverwriteDecision askForMultipleOverwrites(Set<String> existingFiles) {
		if (existingFiles.isEmpty()) {
			return OverwriteDecision.CONTINUE;
		}

		String message = String.format(Configed.getResourceValue("DocumentExport.multipleFilesExist.message"),
				existingFiles.size());

		Object[] options = { Configed.getResourceValue("DocumentExport.buttonOverwriteAll"),
				Configed.getResourceValue("DocumentExport.buttonSkipAll"), Configed.getResourceValue("buttonCancel") };

		int result = DialogUtils.showJListConfirmationDialog(
				Configed.getResourceValue("DocumentExport.multipleFilesExist.title"),
				Configed.getResourceValue("DocumentExport.filesToOverwrite"), message, 30, existingFiles, options);

		return switch (result) {
		case 0 -> OverwriteDecision.OVERWRITE_ALL;
		case 1 -> OverwriteDecision.SKIP_ALL;
		default -> OverwriteDecision.CANCEL;
		};
	}

	protected String checkFile(String filename, FileNameExtensionFilter exFilter, OverwriteDecision decision) {
		if (filename == null) {
			filename = getFileLocation(exFilter);
		} else if (new File(filename).isDirectory()) {
			Logging.error("no valid filename ", filename);
		} else {
			exportDirectory = new File(filename).getParentFile();
		}

		Logging.debug(this, "filename ", filename);

		if (decision == OverwriteDecision.OVERWRITE_ALL) {
			return filename;
		} else if (decision == OverwriteDecision.SKIP_ALL) {
			return null;
		} else {
			// Do nothing.
		}

		if (askForOverwrite && filename != null) {
			filename = askForOverride(filename);
		}

		Logging.debug(this, "export to ", filename);

		return filename;
	}

	private static String askForOverride(String filename) {
		try {
			File file = new File(filename);
			Logging.devel("filename " + filename + " exists " + file.exists());
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

	protected String getFileLocation(FileNameExtensionFilter exFilter) {
		String fileName = null;

		SystemFileChooser fileChooser = new SystemFileChooser(exportDirectory);
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(exFilter);

		fileChooser.setDialogType(SystemFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle(Configed.getResourceValue("DocumentExport.chooser"));
		int returnVal = fileChooser.showDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("buttonOK"));
		if (returnVal == SystemFileChooser.APPROVE_OPTION) {
			fileName = fileChooser.getSelectedFile().getAbsolutePath();
			Logging.info(this, "clicked ok on JFileChosser, get now fileName: ", fileName);

			if (!fileName.toLowerCase(Locale.ROOT).endsWith(extension)) {
				fileName += extension;
			}

			exportDirectory = new File(fileName).getParentFile();
		}

		return fileName;
	}
}
