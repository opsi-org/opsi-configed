/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.util.SystemFileChooser;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.PanelStateSwitch;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class BaseMultiClientReportPanel extends JPanel {
	private JButton buttonExport;
	private ActionListener actionListenerForStart;

	private JCheckBox checkAllowOverwriting;

	private KindOfExport kindOfExport;

	private File exportDirectory;
	private String exportDirectoryS;
	private SystemFileChooser chooserDirectory;
	private JTextField fieldExportDirectory;
	private JTextField fieldFilenamePrefix;

	private String title;
	private String filenamePrefix;
	private String exportKey;
	private String exportDirKey;
	private String exportFilePrefixKey;
	private String exportAllowOverwriteKey;

	private JLabel labelSwauditMultiClientReport1;

	public BaseMultiClientReportPanel() {
		this(Configed.getResourceValue("PanelHWMultiClientReport.title2"),
				Configed.getResourceValue("PanelHWMultiClientReport.filenamePrefix"), "hwaudit_kind_of_export",
				"hwaudit_export_dir", "hwaudit_export_file_prefix", "hwaudit_export_allow_overwrite");
	}

	@SuppressWarnings("java:S1699")
	protected BaseMultiClientReportPanel(String title, String filenamePrefix, String exportKey, String exportDirKey,
			String exportFilePrefixKey, String exportAllowOverwriteKey) {
		this.title = title;
		this.filenamePrefix = filenamePrefix;
		this.exportKey = exportKey;
		this.exportDirKey = exportDirKey;
		this.exportFilePrefixKey = exportFilePrefixKey;
		this.exportAllowOverwriteKey = exportAllowOverwriteKey;

		setupPanel();
	}

	public void setActionListenerForStart(ActionListener li) {
		Logging.info(this, "setActionListenerForStart ", li);
		if (actionListenerForStart != null) {
			buttonExport.removeActionListener(actionListenerForStart);
		}

		if (li != null) {
			actionListenerForStart = li;
			buttonExport.addActionListener(actionListenerForStart);
		}
	}

	public boolean allowOverwriting() {
		return checkAllowOverwriting.isSelected();
	}

	public KindOfExport getKindOfExport() {
		return kindOfExport;
	}

	public String getExportDirectory() {
		return fieldExportDirectory.getText();
	}

	public String getExportfilePrefix() {
		return fieldFilenamePrefix.getText();
	}

	private void setupPanel() {
		this.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));

		labelSwauditMultiClientReport1 = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.title"));
		JLabel labelSwauditMultiClientReport2 = new JLabel(title);

		this.add(labelSwauditMultiClientReport1);
		this.add(labelSwauditMultiClientReport2);

		JPanel subpanelPreConfig = setupSubPanelPreConfig();
		this.add(subpanelPreConfig, "growx, pushx, gaptop " + Globals.GAP_SIZE + ", gapbottom " + Globals.GAP_SIZE);

		buttonExport = new JButton(Configed.getResourceValue("PanelSWMultiClientReport.start"));
		this.add(buttonExport, "gaptop " + Globals.GAP_SIZE);
	}

	private JPanel setupSubPanelPreConfig() {
		JPanel panel = new JPanel(new MigLayout("insets " + Globals.GAP_SIZE + ", hidemode 2", "[grow]", "[]0"));

		addExportDirectory(panel);
		addExportType(panel);
		addFilenamePrefix(panel);
		addFilenameInformation(panel);

		addCustomOptions(panel);

		addAllowOverwriting(panel);

		return panel;
	}

	private void addExportDirectory(JPanel panel) {
		JLabel labelExportDirectory = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));

		exportDirectoryS = Configed.getSavedStates().getProperty(exportDirKey, "");

		File dir = exportDirectoryS.isEmpty() ? null : new File(exportDirectoryS);

		exportDirectory = (dir != null && dir.isDirectory()) ? dir
				: new File(System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY));

		chooserDirectory = new SystemFileChooser();
		chooserDirectory.setFileHidingEnabled(false);
		chooserDirectory.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);

		fieldExportDirectory = new JTextField(exportDirectoryS);
		fieldExportDirectory.setEditable(false);

		JButton buttonSelectDir = new JButton(Icons.getIntellijIcon("open"));
		buttonSelectDir.setToolTipText(Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));
		buttonSelectDir.addActionListener(e -> buttonCallSelectExportDirectory());

		panel.add(labelExportDirectory, "split 3");
		panel.add(buttonSelectDir, "gapleft " + Globals.GAP_SIZE + ", wmin 0, wmax pref");
		panel.add(fieldExportDirectory, "span, growx, pushx, wrap");
	}

	private void addExportType(JPanel panel) {
		PanelStateSwitch<KindOfExport> panelSelectExportType = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelSWMultiClientReport.selectExportType"), KindOfExport.PDF,
				KindOfExport.values(), KindOfExport.class, (Enum<KindOfExport> val) -> {
					kindOfExport = (KindOfExport) val;
					Configed.getSavedStates().setProperty(exportKey, "" + val);
				});

		panelSelectExportType.setValueByString(Configed.getSavedStates().getProperty(exportKey));

		kindOfExport = (KindOfExport) panelSelectExportType.getValue();

		panel.add(panelSelectExportType, "span, growx, pushx, gaptop " + Globals.MIN_GAP_SIZE + ", wrap");
	}

	private void addFilenamePrefix(JPanel panel) {
		JLabel labelFilenamePrefix = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenamePrefix"));

		fieldFilenamePrefix = new JTextField(
				Configed.getSavedStates().getProperty(exportFilePrefixKey, filenamePrefix));

		panel.add(labelFilenamePrefix, "split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		panel.add(fieldFilenamePrefix, "span, growx, pushx, wrap");
	}

	private static void addFilenameInformation(JPanel panel) {
		JLabel labelFilenameInformation = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenameInformation"));

		panel.add(labelFilenameInformation, "span, gaptop " + Globals.MIN_GAP_SIZE + ", wrap");
	}

	private void addAllowOverwriting(JPanel panel) {
		JLabel labelAllowOverwriting = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.askForOverwrite"));

		checkAllowOverwriting = new JCheckBox();
		checkAllowOverwriting.addItemListener(e -> Configed.getSavedStates().setProperty(exportAllowOverwriteKey,
				Boolean.toString(checkAllowOverwriting.isSelected())));

		panel.add(checkAllowOverwriting, "split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		panel.add(labelAllowOverwriting, "gaptop " + Globals.MIN_GAP_SIZE + ", wrap");
	}

	public void updateTitle1Label(int size, String extraInfo) {
		if (size == 0) {
			labelSwauditMultiClientReport1.setText(Configed.getResourceValue("PanelSWMultiClientReport.title1"));
		} else {
			labelSwauditMultiClientReport1.setText(String.format(extraInfo,
					Configed.getResourceValue("PanelSWMultiClientReport.title1").replace(":", ""), size));
		}
	}

	protected void addCustomOptions(JPanel panel) {
		// default: no extra options
	}

	public void buttonCallSelectExportDirectory() {
		chooserDirectory.setCurrentDirectory(exportDirectory);

		if (chooserDirectory.showOpenDialog(this) == SystemFileChooser.APPROVE_OPTION) {
			exportDirectory = chooserDirectory.getSelectedFile();
			Logging.info(this, "selected directory ", exportDirectory);

			if (exportDirectory != null) {
				exportDirectoryS = exportDirectory.toString();
			}

			fieldExportDirectory.setText(exportDirectoryS);

			Configed.getSavedStates().setProperty("hwaudit_export_dir", exportDirectoryS);
		}
	}
}
