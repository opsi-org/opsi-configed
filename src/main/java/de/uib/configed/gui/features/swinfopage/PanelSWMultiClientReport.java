/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

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
import de.uib.configed.gui.share.swing.PanelStateSwitch;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelSWMultiClientReport extends JPanel {
	private JButton buttonStart;
	private ActionListener actionListenerForStart;

	private JCheckBox checkWithMsUpdates;
	private JCheckBox checkWithMsUpdates2;
	private JCheckBox checkAskForOverwrite;

	private KindOfExport kindOfExport;

	private File exportDirectory;
	private String exportDirectoryS;
	private SystemFileChooser chooserDirectory;
	private JTextField fieldExportDirectory;
	private JTextField fieldFilenamePrefix;

	public PanelSWMultiClientReport() {
		setupPanel();
	}

	public void setActionListenerForStart(ActionListener li) {
		Logging.info(this, "setActionListenerForStart ", li);
		if (actionListenerForStart != null) {
			buttonStart.removeActionListener(actionListenerForStart);
		}

		if (li != null) {
			actionListenerForStart = li;
			buttonStart.addActionListener(actionListenerForStart);
		}
	}

	public boolean wantsWithMsUpdates() {
		return checkWithMsUpdates.isSelected();
	}

	public boolean wantsWithMsUpdates2() {
		return checkWithMsUpdates2.isSelected();
	}

	public boolean wantsAskForOverwrite() {
		return checkAskForOverwrite.isSelected();
	}

	public KindOfExport wantsKindOfExport() {
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

		JLabel labelSwauditMultiClientReport1 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title1"));
		JLabel labelSwauditMultiClientReport2 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title2"));

		this.add(labelSwauditMultiClientReport1);
		this.add(labelSwauditMultiClientReport2);

		JPanel subpanelPreConfig = setupSubPanelPreConfig();
		this.add(subpanelPreConfig, "growx, pushx, gaptop " + Globals.GAP_SIZE + ", gapbottom " + Globals.GAP_SIZE);

		this.add(buttonStart, "gaptop " + Globals.GAP_SIZE);
	}

	private JPanel setupSubPanelPreConfig() {
		JLabel labelFilenamePrefix = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenamePrefix"));

		String filenamePrefix = Configed.getSavedStates().getProperty("swaudit_export_file_prefix",
				Configed.getResourceValue("PanelSWMultiClientReport.filenamePrefix"));

		fieldFilenamePrefix = new JTextField(filenamePrefix);
		fieldFilenamePrefix.setEditable(true);
		JLabel labelFilenameInformation = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenameInformation"));

		JLabel labelAskForOverwrite = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.askForOverwrite"));

		checkAskForOverwrite = new JCheckBox();

		buttonStart = new JButton(Configed.getResourceValue("PanelSWMultiClientReport.start"));

		exportDirectoryS = Configed.getSavedStates().getProperty("swaudit_export_dir", "");

		File dir = exportDirectoryS.isEmpty() ? null : new File(exportDirectoryS);

		// We will get the default directory from user.dir if the saved one is invalid
		exportDirectory = (dir != null && dir.isDirectory()) ? dir
				: new File(System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY));

		chooserDirectory = new SystemFileChooser();
		chooserDirectory.setFileHidingEnabled(false);
		chooserDirectory.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);

		fieldExportDirectory = new JTextField(exportDirectoryS);
		fieldExportDirectory.setEditable(false);

		JLabel labelExportDirectory = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));
		exportDirectoryS = "";

		JButton buttonCallSelectExportDirectory = new JButton(Icons.getIntellijIcon("open"));
		buttonCallSelectExportDirectory
				.setToolTipText(Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));

		buttonCallSelectExportDirectory.addActionListener(actionEvent -> buttonCallSelectExportDirectory());

		JLabel labelWithMsUpdates = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates"));

		JLabel labelWithMsUpdates2 = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates2"));

		checkWithMsUpdates = new JCheckBox();
		checkWithMsUpdates2 = new JCheckBox();

		PanelStateSwitch<KindOfExport> panelSelectExportType = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelSWMultiClientReport.selectExportType"), KindOfExport.PDF,
				KindOfExport.values(), KindOfExport.class, ((Enum<KindOfExport> val) -> {
					Logging.info(this, "change to ", val);
					kindOfExport = (KindOfExport) val;
					Configed.getSavedStates().setProperty("swaudit_kind_of_export", "" + val);
				}));

		panelSelectExportType.setValueByString(Configed.getSavedStates().getProperty("swaudit_kind_of_export"));

		kindOfExport = (KindOfExport) panelSelectExportType.getValue();

		Logging.info(this, "kindOfExport   ", kindOfExport);

		JPanel subpanelPreConfig = new JPanel(
				new MigLayout("insets " + Globals.GAP_SIZE + ", hidemode 2", "[grow]", "[]0"));

		subpanelPreConfig.add(labelExportDirectory, "split 3");
		subpanelPreConfig.add(buttonCallSelectExportDirectory, "gapleft " + Globals.GAP_SIZE + ", wmin 0, wmax pref");
		subpanelPreConfig.add(fieldExportDirectory, "span, growx, pushx, wrap");

		subpanelPreConfig.add(panelSelectExportType, "span, growx, pushx, gaptop " + Globals.MIN_GAP_SIZE + ", wrap");

		subpanelPreConfig.add(labelFilenamePrefix,
				"split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		subpanelPreConfig.add(fieldFilenamePrefix, "span, growx, pushx, wrap");

		subpanelPreConfig.add(labelFilenameInformation, "span, gaptop " + Globals.MIN_GAP_SIZE + ", wrap");

		subpanelPreConfig.add(checkWithMsUpdates,
				"split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		subpanelPreConfig.add(labelWithMsUpdates, "align left, gaptop " + Globals.MIN_GAP_SIZE + ", wrap");

		subpanelPreConfig.add(checkWithMsUpdates2, "split 2, gapright " + Globals.GAP_SIZE);
		subpanelPreConfig.add(labelWithMsUpdates2, "align left, wrap");

		subpanelPreConfig.add(checkAskForOverwrite,
				"split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		subpanelPreConfig.add(labelAskForOverwrite, "gaptop " + Globals.MIN_GAP_SIZE + ", wrap");

		return subpanelPreConfig;
	}

	private void buttonCallSelectExportDirectory() {
		chooserDirectory.setCurrentDirectory(exportDirectory);

		if (chooserDirectory.showOpenDialog(this) == SystemFileChooser.APPROVE_OPTION) {
			exportDirectory = chooserDirectory.getSelectedFile();
			Logging.info(this, "selected directory ", exportDirectory);

			if (exportDirectory != null) {
				exportDirectoryS = exportDirectory.toString();
			}

			fieldExportDirectory.setText(exportDirectoryS);

			Configed.getSavedStates().setProperty("swaudit_export_dir", exportDirectoryS);
		}
	}
}
