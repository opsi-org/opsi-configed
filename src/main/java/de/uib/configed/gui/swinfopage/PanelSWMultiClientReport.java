package de.uib.configed.gui.swinfopage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JTextShowField;
import de.uib.utilities.swing.PanelStateSwitch;

public class PanelSWMultiClientReport extends JPanel {
	JButton buttonStart;
	ActionListener actionListenerForStart;

	boolean withMsUpdates = false;
	boolean withMsUpdates2 = false;
	boolean askForOverwrite = false;

	public static final String filenamePrefixDefault = "report_";

	protected PanelStateSwitch panelSelectExportType;
	protected PanelSWInfo.KindOfExport kindOfExport;

	java.io.File exportDirectory;
	String exportDirectoryS;
	JFileChooser chooserDirectory;
	JTextShowField fieldExportDirectory;
	JTextShowField fieldFilenamePrefix;

	JCheckBox checkWithMsUpdates;
	JCheckBox checkWithMsUpdates2;
	JCheckBox checkAskForOverwrite;

	public PanelSWMultiClientReport() {
		setupPanel();
	}

	public void setActionListenerForStart(ActionListener li) {
		logging.info(this, "setActionListenerForStart " + li);
		if (actionListenerForStart != null)
			buttonStart.removeActionListener(actionListenerForStart);
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

	protected void setupPanel() {

		setBackground(Globals.backLightBlue);
		GroupLayout glGlobal = new GroupLayout(this);
		this.setLayout(glGlobal);

		JLabel labelSwauditMultiClientReport1 = new JLabel(
				configed.getResourceValue("PanelSWMultiClientReport.title1"));
		labelSwauditMultiClientReport1.setFont(de.uib.utilities.Globals.defaultFontBig);

		JLabel labelSwauditMultiClientReport2 = new JLabel(
				configed.getResourceValue("PanelSWMultiClientReport.title2"));
		labelSwauditMultiClientReport2.setFont(de.uib.utilities.Globals.defaultFontBold);

		JLabel labelFilenamePrefix = new JLabel(
				configed.getResourceValue("PanelSWMultiClientReport.labelFilenamePrefix"));

		String filenamePrefix = configed.savedStates.saveSWauditExportFilePrefix.deserialize();

		if (filenamePrefix == null || filenamePrefix.length() == 0)
			filenamePrefix = configed.getResourceValue("PanelSWMultiClientReport.filenamePrefix");

		if (filenamePrefix == null)
			filenamePrefix = filenamePrefixDefault;

		fieldFilenamePrefix = new JTextShowField(filenamePrefix);
		fieldFilenamePrefix.setEditable(true);
		JLabel labelFilenameInformation = new JLabel(
				configed.getResourceValue("PanelSWMultiClientReport.labelFilenameInformation"));

		JLabel labelAskForOverwrite = new JLabel(configed.getResourceValue("PanelSWMultiClientReport.askForOverwrite"));
		labelAskForOverwrite.setFont(Globals.defaultFont);
		checkAskForOverwrite = new JCheckBox("", askForOverwrite);

		checkAskForOverwrite.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				askForOverwrite = checkAskForOverwrite.isSelected();
				logging.info(this, "askForOverwrite new value : " + askForOverwrite);
			}
		});

		buttonStart = new JButton(configed.getResourceValue("PanelSWMultiClientReport.start"));

		exportDirectory = null;

		exportDirectoryS = configed.savedStates.saveSWauditExportDir.deserialize();
		if (exportDirectoryS == null)
			exportDirectoryS = "";

		boolean found = false;
		try {
			if (exportDirectoryS.length() > 0) {
				File f = new File(exportDirectoryS);
				if (f.exists() && f.isDirectory())
					found = true;
			}

			if (found)
				exportDirectory = new File(exportDirectoryS);
			else
				exportDirectory = new File(System.getProperty(logging.envVariableForUserDirectory));
		} catch (Exception ex) {
			logging.warning(this, "could not define exportDirectory)");
		}

		chooserDirectory = new JFileChooser();
		chooserDirectory.setPreferredSize(de.uib.utilities.Globals.filechooserSize);
		chooserDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserDirectory.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserDirectory);
		// chooserDirectory.setControlButtonsAreShown(false);

		fieldExportDirectory = new JTextShowField(exportDirectoryS);

		final JPanel panel = this;

		JLabel labelExportDirectory = new JLabel(
				configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));
		exportDirectoryS = "";

		JButton buttonCallSelectExportDirectory = new JButton("",
				de.uib.configed.Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory
				.setSelectedIcon(de.uib.configed.Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory.setPreferredSize(de.uib.configed.Globals.graphicButtonDimension);
		buttonCallSelectExportDirectory
				.setToolTipText(configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));

		buttonCallSelectExportDirectory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooserDirectory.setCurrentDirectory(exportDirectory);

				int returnVal = chooserDirectory.showOpenDialog(panel);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					exportDirectory = chooserDirectory.getSelectedFile();
					logging.info(this, "selected directory " + exportDirectory);

					if (exportDirectory != null) {
						exportDirectoryS = exportDirectory.toString();
					}

					fieldExportDirectory.setText(exportDirectoryS);
					// getFilepathStart();
					configed.savedStates.saveSWauditExportDir.serialize(exportDirectoryS);

				}

			}
		});

		JLabel labelWithMsUpdates = new JLabel(configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates"));
		labelWithMsUpdates.setFont(Globals.defaultFont);
		JLabel labelWithMsUpdates2 = new JLabel(configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates2"));
		labelWithMsUpdates2.setFont(Globals.defaultFont);

		checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		checkWithMsUpdates.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				withMsUpdates = checkWithMsUpdates.isSelected();
				logging.info(this, "withMsUpdates new value : " + withMsUpdates);
			}
		});

		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		checkWithMsUpdates2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				withMsUpdates2 = checkWithMsUpdates2.isSelected();
				logging.info(this, "withMsUpdates2 new value : " + withMsUpdates2);
			}
		});

		panelSelectExportType = new PanelStateSwitch(
				configed.getResourceValue("PanelSWMultiClientReport.selectExportType"),
				(Enum) PanelSWInfo.KindOfExport.PDF, PanelSWInfo.KindOfExport.values(), PanelSWInfo.KindOfExport.class,
				(val -> {
					logging.info(this, "change to " + val);
					kindOfExport = (PanelSWInfo.KindOfExport) val;
					configed.savedStates.saveSWauditKindOfExport.serialize("" + val);
				}));

		// configed.savedStates.saveSWauditKindOfExport.serialize("CSV");

		String koe = configed.savedStates.saveSWauditKindOfExport.deserialize();
		panelSelectExportType.setValueByString(koe);

		kindOfExport = (PanelSWInfo.KindOfExport) panelSelectExportType.getValue();

		logging.info(this, "kindOfExport set from savedStates  " + koe);
		logging.info(this, "kindOfExport   " + kindOfExport);

		// System.exit(0);

		JPanel subpanelPreConfig = new JPanel();
		subpanelPreConfig.setBackground(Globals.backgroundWhite);// Globals.backNimbusLight);

		GroupLayout glPreConfig = new GroupLayout(subpanelPreConfig);
		subpanelPreConfig.setLayout(glPreConfig);
		glPreConfig.setVerticalGroup(glPreConfig.createSequentialGroup()

				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelExportDirectory, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(buttonCallSelectExportDirectory, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight)
						.addComponent(fieldExportDirectory, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)

				.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFilenamePrefix, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(fieldFilenamePrefix, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelFilenameInformation, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(
						glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelWithMsUpdates, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight)
								.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMsUpdates2, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAskForOverwrite, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize));

		glPreConfig.setHorizontalGroup(glPreConfig.createParallelGroup()

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(labelExportDirectory, de.uib.configed.Globals.buttonWidth,
								de.uib.configed.Globals.buttonWidth * 2, de.uib.configed.Globals.buttonWidth * 2)

						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)

						.addComponent(buttonCallSelectExportDirectory, Globals.graphicButtonWidth,
								Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(fieldExportDirectory, 40, de.uib.configed.Globals.buttonWidth * 2,
								Short.MAX_VALUE)
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(labelFilenamePrefix, de.uib.configed.Globals.buttonWidth,
								de.uib.configed.Globals.buttonWidth * 2, de.uib.configed.Globals.buttonWidth * 2)

						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)

						.addGap(Globals.graphicButtonWidth, Globals.graphicButtonWidth, Globals.graphicButtonWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(fieldFilenamePrefix, de.uib.configed.Globals.buttonWidth / 2,
								de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE / 2)
						.addGap(20, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE / 2)
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(labelFilenameInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(
						glPreConfig.createSequentialGroup()
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
								.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(labelWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(
						glPreConfig.createSequentialGroup()
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
								.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(labelWithMsUpdates2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize).addComponent(labelAskForOverwrite,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

		);

		glGlobal.setVerticalGroup(glGlobal.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport1, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport2, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)

				.addComponent(subpanelPreConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)

				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonStart,
						Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))

		);

		glGlobal.setHorizontalGroup(
				glGlobal.createParallelGroup().addGap(3 * Globals.hGapSize, 3 * Globals.hGapSize, 3 * Globals.hGapSize)
						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
								.addComponent(labelSwauditMultiClientReport1, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE))

						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
								.addComponent(labelSwauditMultiClientReport2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE))
						.addGroup(glGlobal.createSequentialGroup()
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(subpanelPreConfig, 40, 40, Short.MAX_VALUE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize))

						.addGroup(glGlobal.createSequentialGroup()
								.addGap(2 * Globals.hGapSize, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
								.addComponent(buttonStart, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(3 * Globals.hGapSize, 3 * Globals.hGapSize, 3 * Globals.hGapSize));
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {
			logging.debug(" kein Nimbus " + ex);
		}
		JFrame f = new JFrame();
		f.getContentPane().add(new PanelSWMultiClientReport());
		f.setSize(new Dimension(700, 600));
		f.setVisible(true);

	}
}