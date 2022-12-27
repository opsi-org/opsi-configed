package de.uib.utilities.table;

import java.awt.HeadlessException;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.table.gui.PanelGenEditTable;

public abstract class ExportTable {
	protected javax.swing.JTable theTable;

	protected Vector<String> classNames;
	protected Map<String, String> metaData;

	protected javax.swing.filechooser.FileNameExtensionFilter extensionFilter;
	protected String defaultExportFilename;

	protected File exportDirectory;

	protected Vector<Integer> excludeCols;

	protected boolean askForOverwrite;

	protected String writeToFile;
	protected JFileChooser chooser;

	protected String client;
	protected String title;
	protected String subtitle;

	protected String extension;

	DecimalFormat f = new DecimalFormat("#0.00");

	protected final String SHORT_APPNAME = Globals.APPNAME;

	public ExportTable(javax.swing.JTable table, Vector<String> classNames) {
		this.theTable = table;
		this.classNames = classNames;
		askForOverwrite = true;
	}

	public ExportTable(javax.swing.JTable table) {
		this(table, null);
	}

	public void setTableAndClassNames(javax.swing.JTable table, Vector<String> classNames) {
		this.theTable = table;
		this.classNames = classNames;
	}

	public void setClassNames(Vector<String> classNames) {
		this.classNames = classNames;
	}

	public void setAskForOverwrite(boolean b) {
		askForOverwrite = b;
	}

	public void setExcludeCols(Vector<Integer> excludeCols)
	// only take into account for excel export at the moment
	{
		this.excludeCols = excludeCols;
	}

	protected abstract void execute(String fileName, boolean onlySelectedRows);

	public JMenuItemFormatted getMenuItemExport() {
		JMenuItemFormatted menuItem = new JMenuItemFormatted(
				configed.getResourceValue("PanelGenEditTable.exportTableAsCSV")

		);
		menuItem.addActionListener(actionEvent -> execute(null, false));
		return menuItem;

	}

	public void addMenuItemsTo(JPopupMenu popup) {
		popup.add(getMenuItemExport());
		popup.add(getMenuItemExportSelected());
	}

	public JMenuItemFormatted getMenuItemExportSelected() {
		JMenuItemFormatted menuItem = new JMenuItemFormatted(
				configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV")

		);
		menuItem.addActionListener(actionEvent -> {
			boolean onlySelected = true;
			logging.debug(this, "menuItemExportSelectedCSV " + onlySelected);
			execute(null, onlySelected);
		});
		return menuItem;
	}

	public void setPanelTable(PanelGenEditTable panelTable) {
		theTable = panelTable.getTheTable();
	}

	public void setClient(String clientID) {

		client = clientID;
	}

	public void setMetaData(HashMap<String, String> tableMetaData) {

		metaData = tableMetaData;
	}

	protected String checkExtension(String path) {

		if (path == null)
			return null;

		if (!path.toLowerCase().endsWith(extension))
			path = path + extension;

		return path;
	}

	protected Boolean checkSelection(boolean onlySelectedRows) {
		Boolean result = onlySelectedRows;

		if (onlySelectedRows) {
			logging.debug("selectedRows: " + theTable.getSelectedRows().length);
			if (theTable.getRowCount() > 0 && theTable.getSelectedRows().length == 0) {

				FTextArea fChoice = new FTextArea(null,
						SHORT_APPNAME + " " + configed.getResourceValue("ExportTable.title"), true,
						new String[] {
								configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly"),
								configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCompleteTable"),
								configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCancel") },

						500, 200);
				fChoice.setDefaultResult(3);
				fChoice.setMessage(configed.getResourceValue("ExportTable.caseNoSelectedRows.info") + "\n\n\n"
						+ configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportHeaderOnly.text")
						+ "\n\n"
						+ configed.getResourceValue("ExportTable.caseNoSelectedRows.option.exportCompleteTable.text")
						+ "\n\n");
				fChoice.setVisible(true);

				int answer = fChoice.getResult();

				result = null;
				logging.info(this, "checkSelection answered " + answer);
				switch (answer) {
				case 1:
					result = true;
					break;
				case 2:
					result = false;
					break;
				}

			}
		}

		logging.info(this, "checkSelection gives: onlySelectedRows = " + result);

		return result;
	}

	protected String checkFile(String filename, javax.swing.filechooser.FileNameExtensionFilter exFilter) {
		if (filename == null) {
			JFileChooser chooser = new JFileChooser(exportDirectory);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			chooser.addChoosableFileFilter(exFilter);
			chooser.setPreferredSize(Globals.filechooserSize);

			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(SHORT_APPNAME + "    " + configed.getResourceValue("DocumentExport.chooser"));

			chooser.setApproveButtonText("ok");
			chooser.setApproveButtonToolTipText(configed.getResourceValue("ExportTable.approveTooltip"));
			UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
			UIManager.put("FileChooser.cancelButtonToolTipText", "");

			UIManager.put("FileChooser.lookInLabelText", "Suchen in:");

			SwingUtilities.updateComponentTreeUI(chooser);

			int returnVal = chooser.showDialog(Globals.frame1, null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					filename = chooser.getSelectedFile().getAbsolutePath();

					File file = new File(filename);

					if (file.isDirectory())
						filename = filename + File.separator + defaultExportFilename;

					else {
						if (!filename.toLowerCase().endsWith(".csv"))
							filename = filename + ".csv";
					}

					logging.debug(this, "filename " + filename);

					file = new File(filename);

					if (file.exists() && askForOverwrite) {
						int option = JOptionPane.showConfirmDialog(Globals.frame1,
								configed.getResourceValue("DocumentExport.showConfirmDialog") + "\n" + file.getName(),
								Globals.APPNAME + " " + configed.getResourceValue("DocumentExport.question"),
								JOptionPane.OK_CANCEL_OPTION);

						if (option == JOptionPane.CANCEL_OPTION)
							filename = null;
					}
				} catch (Exception fc_e) {
					logging.error(configed.getResourceValue("DocumentExport.errorNoValidFilename") + "\n" + filename);
				}
			}
		}

		if (filename != null) {
			try {
				exportDirectory = new File(filename).getParentFile();
			} catch (Exception e) {
				filename = null;
				logging.error("Problem mit dem Verzeichnis von " + filename + " : " + e);
			}
		}

		logging.debug(this, "export to " + filename);

		return filename;
	}

	protected String getFileLocation() {
		String fileName = null;

		logging.info(this, "getFileLocation with writeToFile " + writeToFile);

		File defaultFile = new File(writeToFile);

		chooser = new JFileChooser(exportDirectory);
		chooser.setPreferredSize(Globals.filechooserSize);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf"));

		chooser.setSelectedFile(defaultFile);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Globals.APPNAME + " " + configed.getResourceValue("DocumentExport.chooser"));

		int returnVal = chooser.showDialog(Globals.mainContainer, "OK");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();

		}

		if (fileName != null) {
			try {
				exportDirectory = new File(fileName).getParentFile();
			} catch (Exception ex) {
				logging.error("directory not found for " + fileName + " : " + ex);
			}
		}
		return fileName;
	}

	protected String checkFileForExistence(String filename) {
		String result = null;
		try {
			logging.info(this, "checkFileForExistence " + filename + " askForOverwrite " + askForOverwrite);

			if (!askForOverwrite)
				return filename;

			File file = new File(filename);

			boolean fileExists = file.exists();

			if (!fileExists)
				return filename;

			int option = JOptionPane.showConfirmDialog(Globals.mainContainer,
					configed.getResourceValue("DocumentExport.showConfirmDialog") + "\n" + file.getName(),
					Globals.APPNAME + " " + configed.getResourceValue("DocumentExport.question"),
					JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.CANCEL_OPTION)
				result = null;
			else
				result = filename;
		} catch (HeadlessException ex) {
			logging.error(configed.getResourceValue("DocumentExport.errorNoValidFilename") + "\n" + filename);

		}

		return result;
	}

}