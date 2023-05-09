package de.uib.configed.gui.swinfopage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.swinfopage.PanelSWInfo.KindOfExport;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JTextShowField;
import de.uib.utilities.swing.PanelStateSwitch;

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

	private JCheckBox checkWithMsUpdates;
	private JCheckBox checkWithMsUpdates2;
	private JCheckBox checkAskForOverwrite;

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

		if (!ConfigedMain.THEMES) {
			setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout glGlobal = new GroupLayout(this);
		this.setLayout(glGlobal);

		JLabel labelSwauditMultiClientReport1 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title1"));
		labelSwauditMultiClientReport1.setFont(Globals.defaultFontBig);

		JLabel labelSwauditMultiClientReport2 = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.title2"));
		labelSwauditMultiClientReport2.setFont(Globals.defaultFontBold);

		JLabel labelFilenamePrefix = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelFilenamePrefix"));

		String filenamePrefix = Configed.savedStates.saveSWauditExportFilePrefix.deserialize();

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
		labelAskForOverwrite.setFont(Globals.defaultFont);
		checkAskForOverwrite = new JCheckBox("", askForOverwrite);

		checkAskForOverwrite.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				askForOverwrite = checkAskForOverwrite.isSelected();
				Logging.info(this, "askForOverwrite new value : " + askForOverwrite);
			}
		});

		buttonStart = new JButton(Configed.getResourceValue("PanelSWMultiClientReport.start"));

		exportDirectory = null;

		exportDirectoryS = Configed.savedStates.saveSWauditExportDir.deserialize();
		if (exportDirectoryS == null) {
			exportDirectoryS = "";
		}

		boolean found = false;
		try {
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
		} catch (Exception ex) {
			Logging.warning(this, "could not define exportDirectory)", ex);
		}

		chooserDirectory = new JFileChooser();
		chooserDirectory.setPreferredSize(Globals.filechooserSize);
		chooserDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserDirectory.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserDirectory);

		fieldExportDirectory = new JTextShowField(exportDirectoryS);

		final JPanel panel = this;

		JLabel labelExportDirectory = new JLabel(
				Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));
		exportDirectoryS = "";

		JButton buttonCallSelectExportDirectory = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectExportDirectory.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectExportDirectory
				.setToolTipText(Configed.getResourceValue("PanelSWMultiClientReport.labelExportDirectory"));

		buttonCallSelectExportDirectory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooserDirectory.setCurrentDirectory(exportDirectory);

				int returnVal = chooserDirectory.showOpenDialog(panel);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					exportDirectory = chooserDirectory.getSelectedFile();
					Logging.info(this, "selected directory " + exportDirectory);

					if (exportDirectory != null) {
						exportDirectoryS = exportDirectory.toString();
					}

					fieldExportDirectory.setText(exportDirectoryS);

					Configed.savedStates.saveSWauditExportDir.serialize(exportDirectoryS);

				}

			}
		});

		JLabel labelWithMsUpdates = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates"));
		labelWithMsUpdates.setFont(Globals.defaultFont);
		JLabel labelWithMsUpdates2 = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates2"));
		labelWithMsUpdates2.setFont(Globals.defaultFont);

		checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		checkWithMsUpdates.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				withMsUpdates = checkWithMsUpdates.isSelected();
				Logging.info(this, "withMsUpdates new value : " + withMsUpdates);
			}
		});

		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		checkWithMsUpdates2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				withMsUpdates2 = checkWithMsUpdates2.isSelected();
				Logging.info(this, "withMsUpdates2 new value : " + withMsUpdates2);
			}
		});

		PanelStateSwitch<KindOfExport> panelSelectExportType = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelSWMultiClientReport.selectExportType"), PanelSWInfo.KindOfExport.PDF,
				PanelSWInfo.KindOfExport.values(), PanelSWInfo.KindOfExport.class, ((Enum<KindOfExport> val) -> {
					Logging.info(this, "change to " + val);
					kindOfExport = (PanelSWInfo.KindOfExport) val;
					Configed.savedStates.saveSWauditKindOfExport.serialize("" + val);
				}));

		String koe = Configed.savedStates.saveSWauditKindOfExport.deserialize();
		panelSelectExportType.setValueByString(koe);

		kindOfExport = (PanelSWInfo.KindOfExport) panelSelectExportType.getValue();

		Logging.info(this, "kindOfExport set from savedStates  " + koe);
		Logging.info(this, "kindOfExport   " + kindOfExport);

		JPanel subpanelPreConfig = new JPanel();
		if (!ConfigedMain.THEMES) {
			subpanelPreConfig.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		GroupLayout glPreConfig = new GroupLayout(subpanelPreConfig);
		subpanelPreConfig.setLayout(glPreConfig);
		glPreConfig.setVerticalGroup(glPreConfig.createSequentialGroup()

				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelExportDirectory, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectExportDirectory, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldExportDirectory, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)

				.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelFilenamePrefix, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldFilenamePrefix, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)

				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelFilenameInformation, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMsUpdates, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMsUpdates2, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(glPreConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelAskForOverwrite, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		glPreConfig.setHorizontalGroup(glPreConfig.createParallelGroup()

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(labelExportDirectory, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)

						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)

						.addComponent(buttonCallSelectExportDirectory, Globals.GRAPHIC_BUTTON_WIDTH,
								Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(fieldExportDirectory, 40, Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(panelSelectExportType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(labelFilenamePrefix, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
								Globals.BUTTON_WIDTH * 2)

						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)

						.addGap(Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
								Globals.GRAPHIC_BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(fieldFilenamePrefix, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Short.MAX_VALUE / 2)
						.addGap(20, Globals.BUTTON_WIDTH, Short.MAX_VALUE / 2)
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(labelFilenameInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(glPreConfig.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(checkAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelAskForOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

		);

		glGlobal.setVerticalGroup(glGlobal.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport1, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						labelSwauditMultiClientReport2, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)

				.addComponent(subpanelPreConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)

				.addGroup(glGlobal.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(buttonStart,
						Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))

		);

		glGlobal.setHorizontalGroup(glGlobal.createParallelGroup()
				.addGap(3 * Globals.HGAP_SIZE, 3 * Globals.HGAP_SIZE, 3 * Globals.HGAP_SIZE)
				.addGroup(glGlobal.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(labelSwauditMultiClientReport1, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))

				.addGroup(glGlobal.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(labelSwauditMultiClientReport2, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(
						glGlobal.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(subpanelPreConfig, 40, 40, Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

				.addGroup(glGlobal.createSequentialGroup()
						.addGap(2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(buttonStart, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(3 * Globals.HGAP_SIZE, 3 * Globals.HGAP_SIZE, 3 * Globals.HGAP_SIZE));
	}
}
