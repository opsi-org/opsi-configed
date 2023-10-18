/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.swinfopage.PanelSWInfo.KindOfExport;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JTextShowField;
import de.uib.utilities.swing.PanelStateSwitch;
import utils.Utils;

public class PanelSWMultiClientReport extends JPanel {

	public static final String FILENAME_PREFIX_DEFAULT = "report_";

	private JButton buttonStart;
	private ActionListener actionListenerForStart;

	private boolean withMsUpdates;
	private boolean withMsUpdates2;
	private boolean askForOverwrite;

	private PanelSWInfo.KindOfExport kindOfExport;

	private File exportDirectory;
	private String exportDirectoryS;
	private JFileChooser chooserDirectory;
	private JTextShowField fieldExportDirectory;
	private JTextShowField fieldFilenamePrefix;

	public PanelSWMultiClientReport() {
		setupPanel();
	}

	public void setActionListenerForStart(ActionListener li) {
		Logging.info(this, "setActionListenerForStart " + li);
		if (actionListenerForStart != null) {
			buttonStart.removeActionListener(actionListenerForStart);
		}

		if (li != null) {
			actionListenerForStart = li;
			buttonStart.addActionListener(actionListenerForStart);
		}
	}

	public boolean wantsWithMsUpdates() {
		return withMsUpdates;
	}

	public boolean wantsWithMsUpdates2() {
		return withMsUpdates2;
	}

	public boolean wantsAskForOverwrite() {
		return askForOverwrite;
	}

	public PanelSWInfo.KindOfExport wantsKindOfExport() {
		return kindOfExport;
	}

	public String getExportDirectory() {
		return fieldExportDirectory.getText();
	}

	public String getExportfilePrefix() {
		return fieldFilenamePrefix.getText();
	}

	private void setupPanel() {

		GroupLayout glGlobal = new GroupLayout(this);
		this.setLayout(glGlobal);

		JLabel labelSwauditMultiClientReport1 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title1"));

		JLabel labelSwauditMultiClientReport2 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title2"));

		JLabel labelFilenamePrefix = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenamePrefix"));

		String filenamePrefix = Configed.getSavedStates().getProperty("swaudit_export_file_prefix");

		if (filenamePrefix == null || filenamePrefix.length() == 0) {
			filenamePrefix = Configed.getResourceValue("PanelSWMultiClientReport.filenamePrefix");
		}

		if (filenamePrefix == null) {
			filenamePrefix = FILENAME_PREFIX_DEFAULT;
		}

		fieldFilenamePrefix = new JTextShowField(filenamePrefix);
		fieldFilenamePrefix.setEditable(true);
		JLabel labelFilenameInformation = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenameInformation"));

		JLabel labelAskForOverwrite = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.askForOverwrite"));

		JCheckBox checkAskForOverwrite = new JCheckBox("", askForOverwrite);

		checkAskForOverwrite.addItemListener((ItemEvent e) -> {
			askForOverwrite = checkAskForOverwrite.isSelected();
			Logging.info(this, "askForOverwrite new value : " + askForOverwrite);
		});

		buttonStart = new JButton(Configed.getResourceValue("PanelSWMultiClientReport.start"));

		exportDirectory = null;

		exportDirectoryS = Configed.getSavedStates().getProperty("swaudit_export_dir");
		if (exportDirectoryS == null) {
			exportDirectoryS = "";
		}

		boolean found = false;

		if (exportDirectoryS.length() > 0) {
			File f = new File(exportDirectoryS);
			if (f.exists() && f.isDirectory()) {
				found = true;
			}
		}

		if (found) {
			exportDirectory = new File(exportDirectoryS);
		} else {
			exportDirectory = new File(System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY));
		}

		chooserDirectory = new JFileChooser();
		chooserDirectory.setPreferredSize(Globals.FILE_CHOOSER_SIZE);
		chooserDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserDirectory.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		SwingUtilities.updateComponentTreeUI(chooserDirectory);

		fieldExportDirectory = new JTextShowField(exportDirectoryS);

		JLabel labelExportDirectory = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));
		exportDirectoryS = "";

		JButton buttonCallSelectExportDirectory = new JButton("", Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectExportDirectory
				.setToolTipText(Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));

		buttonCallSelectExportDirectory.addActionListener((ActionEvent e) -> buttonCallSelectExportDirectory());

		JLabel labelWithMsUpdates = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates"));

		JLabel labelWithMsUpdates2 = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates2"));

		JCheckBox checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		checkWithMsUpdates.addItemListener((ItemEvent e) -> {
			withMsUpdates = checkWithMsUpdates.isSelected();
			Logging.info(this, "withMsUpdates new value : " + withMsUpdates);
		});

		JCheckBox checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		checkWithMsUpdates2.addItemListener((ItemEvent e) -> {
			withMsUpdates2 = checkWithMsUpdates2.isSelected();
			Logging.info(this, "withMsUpdates2 new value : " + withMsUpdates2);
		});

		PanelStateSwitch<KindOfExport> panelSelectExportType = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelSWMultiClientReport.selectExportType"), PanelSWInfo.KindOfExport.PDF,
				PanelSWInfo.KindOfExport.values(), PanelSWInfo.KindOfExport.class, ((Enum<KindOfExport> val) -> {
					Logging.info(this, "change to " + val);
					kindOfExport = (PanelSWInfo.KindOfExport) val;
					Configed.getSavedStates().setProperty("swaudit_kind_of_export", "" + val);
				}));

		String koe = Configed.getSavedStates().getProperty("swaudit_kind_of_export");
		panelSelectExportType.setValueByString(koe);

		kindOfExport = (PanelSWInfo.KindOfExport) panelSelectExportType.getValue();

		Logging.info(this, "kindOfExport set from savedStates  " + koe);
		Logging.info(this, "kindOfExport   " + kindOfExport);

		JPanel subpanelPreConfig = new JPanel();

		GroupLayout glPreConfig = new GroupLayout(subpanelPreConfig);
		subpanelPreConfig.setLayout(glPreConfig);
		glPreConfig.setVerticalGroup(glPreConfig.createSequentialGroup()

				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelExportDirectory, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectExportDirectory, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldExportDirectory, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)

				.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFilenamePrefix, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldFilenamePrefix, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelFilenameInformation, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMsUpdates, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMsUpdates2, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAskForOverwrite, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		glPreConfig.setHorizontalGroup(glPreConfig.createParallelGroup()

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(labelExportDirectory, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)

						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)

						.addComponent(buttonCallSelectExportDirectory, Globals.GRAPHIC_BUTTON_WIDTH,
								Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(fieldExportDirectory, 40, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(labelFilenamePrefix, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)

						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)

						.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
								Globals.GRAPHIC_BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(fieldFilenamePrefix, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Short.MAX_VALUE / 2)
						.addGap(20, Globals.BUTTON_WIDTH, Short.MAX_VALUE / 2)
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(labelFilenameInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(
						glPreConfig.createSequentialGroup()
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addComponent(labelWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(
						glPreConfig.createSequentialGroup()
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addComponent(labelWithMsUpdates2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE).addComponent(labelAskForOverwrite,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

		);

		glGlobal.setVerticalGroup(glGlobal.createSequentialGroup()
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport1, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport2, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)

				.addComponent(subpanelPreConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)

				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonStart,
						Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))

		);

		glGlobal.setHorizontalGroup(
				glGlobal.createParallelGroup().addGap(3 * Globals.GAP_SIZE, 3 * Globals.GAP_SIZE, 3 * Globals.GAP_SIZE)
						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(labelSwauditMultiClientReport1, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))

						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(labelSwauditMultiClientReport2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
						.addGroup(glGlobal.createSequentialGroup()
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addComponent(subpanelPreConfig, 40, 40, Short.MAX_VALUE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE))

						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(buttonStart, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(3 * Globals.GAP_SIZE, 3 * Globals.GAP_SIZE, 3 * Globals.GAP_SIZE));
	}

	private void buttonCallSelectExportDirectory() {
		chooserDirectory.setCurrentDirectory(exportDirectory);

		int returnVal = chooserDirectory.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			exportDirectory = chooserDirectory.getSelectedFile();
			Logging.info(this, "selected directory " + exportDirectory);

			if (exportDirectory != null) {
				exportDirectoryS = exportDirectory.toString();
			}

			fieldExportDirectory.setText(exportDirectoryS);

			Configed.getSavedStates().setProperty("swaudit_export_dir", exportDirectoryS);
		}
	}
}
