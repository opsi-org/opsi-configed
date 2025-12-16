/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiMakeProductFile;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandTemplate;
import de.uib.configed.gui.share.swing.AutoCompletionComboBox;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class MakeProductFileDialog {
	private static final Pattern tripleSemicolonMatcher = Pattern.compile(";;;");
	private static final String FILE_REPLACEMENT_PATTERN = "*.file.*";
	private static final String REMOVE_EXISTING_FILE_COMMAND = "[ -f " + FILE_REPLACEMENT_PATTERN + " ] &&  rm "
			+ FILE_REPLACEMENT_PATTERN + " && echo \"File " + FILE_REPLACEMENT_PATTERN + " removed\" || echo \"File "
			+ FILE_REPLACEMENT_PATTERN + " does not exist\"";
	private static final String DIRECTORY_REPLACEMENT_PATTERN = "*.dir.*";
	private static final String GET_VERSIONS_COMMAND = "grep version: " + DIRECTORY_REPLACEMENT_PATTERN
			+ " --max-count=2  ";
	private static final String GET_PACKAGE_ID_COMMAND = "grep id: " + DIRECTORY_REPLACEMENT_PATTERN
			+ "OPSI/control --max-count=1";

	private JLabel jLabelProductVersionControlFile;
	private JLabel jLabelPackageVersionControlFile;
	private JTextField jTextFieldPackageVersion;
	private JTextField jTextFieldProductVersion;
	private JComboBox<String> jComboBoxMainDir;
	private JCheckBox jCheckBoxOverwrite;
	private AdvancedOptionsPanel advancedOptionsPanel;

	private JLabel jLabelDir;
	private JButton jButtonSearchDir;
	private JButton jButtonSetRights;
	private JLabel jLabelProductVersion;
	private JLabel jLabelPackageVersion;
	private JLabel jLabelVersionsControlFile;
	private JLabel jLabelVersions;
	private JToggleButton jButtonAdvancedSettings;

	private String filename;
	private ConfigedMain configedMain;
	private CompletionComboButton autocompletion;

	private JDialog dialog;

	public MakeProductFileDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		autocompletion = new CompletionComboButton();
		advancedOptionsPanel = new AdvancedOptionsPanel();
		advancedOptionsPanel.setVisible(false);

		initComponents();
		JPanel panel = initPanel();

		filename = "";

		jComboBoxMainDir.setEnabled(true);

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JButton buttonPackageManager = new JButton(
				Configed.getResourceValue("MakeProductFileDialog.buttonToPackageManager"));
		buttonPackageManager
				.addActionListener(actionEvent -> new PackageManagerInstallParameterDialog(configedMain, filename));

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
				null, new Object[] { buttonExecute, buttonPackageManager, Configed.getResourceValue("buttonCancel") });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MakeProductFileDialog.title"));
		dialog.setModal(false);
		dialog.pack();

		dialog.setVisible(true);

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void initComponents() {
		jLabelDir = new JLabel(Configed.getResourceValue("MakeProductFileDialog.serverDir"));
		jLabelDir.setFont(jLabelDir.getFont().deriveFont(Font.BOLD));

		autocompletion.setCombobox(new AutoCompletionComboBox<>(
				new DefaultComboBoxModel<>(autocompletion.getDefaultValues().toArray(new String[0]))) {
			@Override
			public void setSelectedItem(Object item) {
				super.setSelectedItem(item);
				doSetActionGetVersions();
			}
		});
		autocompletion.initCombobox();
		jComboBoxMainDir = autocompletion.getCombobox();

		jButtonSearchDir = autocompletion.getButton();
		jButtonSearchDir.removeActionListener(jButtonSearchDir.getActionListeners()[0]);
		jButtonSearchDir.addActionListener((ActionEvent actionEvent) -> {
			autocompletion.doButtonAction();
			doSetActionGetVersions();
		});

		jLabelPackageVersion = new JLabel("    " + Configed.getResourceValue("MakeProductFileDialog.packageVersion"));
		jLabelPackageVersion.setFont(jLabelPackageVersion.getFont().deriveFont(Font.BOLD));

		jLabelProductVersion = new JLabel("    " + Configed.getResourceValue("MakeProductFileDialog.productVersion"));
		jLabelProductVersion.setFont(jLabelProductVersion.getFont().deriveFont(Font.BOLD));

		jLabelVersionsControlFile = new JLabel(Configed.getResourceValue("MakeProductFileDialog.versions_controlfile"));
		jLabelVersionsControlFile.setFont(jLabelVersionsControlFile.getFont().deriveFont(Font.BOLD));

		jLabelVersions = new JLabel(Configed.getResourceValue("MakeProductFileDialog.versions"));
		jLabelVersions.setFont(jLabelVersions.getFont().deriveFont(Font.BOLD));

		jLabelProductVersionControlFile = new JLabel();
		jLabelPackageVersionControlFile = new JLabel();
		jTextFieldPackageVersion = new JTextField();

		jTextFieldProductVersion = new JTextField();

		enableTfVersions(false);

		jCheckBoxOverwrite = new JCheckBox(Configed.getResourceValue("MakeProductFileDialog.removeExisting"), true);

		jButtonAdvancedSettings = new JToggleButton(
				Configed.getResourceValue("MakeProductFileDialog.btn_advancedSettings"));

		jButtonAdvancedSettings.addActionListener((ActionEvent event) -> {
			advancedOptionsPanel.setVisible(jButtonAdvancedSettings.isSelected());
			dialog.pack();
		});

		jButtonSetRights = new JButton(Configed.getResourceValue("MakeProductFileDialog.btn_setRights"));
		jButtonSetRights.setToolTipText(Configed.getResourceValue("MakeProductFileDialog.btn_setRights.tooltip"));
		jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jComboBoxMainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(jButtonSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(jLabelProductVersionControlFile, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(jLabelPackageVersionControlFile, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldPackageVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonAdvancedSettings, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(advancedOptionsPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxMainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPackageVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldPackageVersion, GroupLayout.Alignment.LEADING,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonAdvancedSettings, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(advancedOptionsPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		return panel;
	}

	private String doActionGetVersions() {
		String dir = Utils.getServerPathFromWebDAVPath((String) jComboBoxMainDir.getEditor().getItem())
				+ "OPSI/control";
		Logging.info(this, "doActionGetVersions, dir ", dir);
		SingleCommandTemplate getVersions = new SingleCommandTemplate(
				GET_VERSIONS_COMMAND.replace(DIRECTORY_REPLACEMENT_PATTERN, dir));
		CommandExecutor executor = new CommandExecutor(configedMain, getVersions);
		executor.setWithGUI(false);
		Logging.info(this, "doActionGetVersions, command ", getVersions);
		String result = executor.execute();
		Logging.info(this, "doActionGetVersions result ", result);

		if (result == null || result.isEmpty()) {
			Logging.warning(this, "doActionGetVersions, could not find versions in file ", dir,
					".Please check if directory exists and contains the file OPSI/control.\n",
					"Please also check the rights of the file/s.");
		} else {
			String[] versions = result.replace("version: ", "").split("\n");
			Logging.info(this, "doActionGetVersions, getDirectories result ", Arrays.toString(versions));
			if (versions.length < 1) {
				Logging.info(this, "doActionGetVersions, not expected versions array ", Arrays.toString(versions));
				return "";
			}
			return versions[0] + ";;;" + versions[1];
		}
		return "";
	}

	private final void doSetActionGetVersions() {
		String versions = doActionGetVersions();
		if (versions.contains(";;;")) {
			enableTfVersions(true);

			String[] versionArray = tripleSemicolonMatcher.split(versions, 2);

			jTextFieldPackageVersion.setText(versionArray[0]);
			jLabelPackageVersionControlFile.setText(versionArray[0]);

			jTextFieldProductVersion.setText(versionArray[1]);
			jLabelProductVersionControlFile.setText(versionArray[1]);
		} else {
			enableTfVersions(false);
			jTextFieldPackageVersion.setText("");
			jLabelPackageVersionControlFile.setText("");
			jTextFieldProductVersion.setText("");
			jLabelProductVersionControlFile.setText("");
		}
	}

	private void enableTfVersions(boolean enable) {
		jTextFieldPackageVersion.setEnabled(enable);
		jTextFieldProductVersion.setEnabled(enable);
	}

	private void doExecSetRights() {
		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		SingleCommandOpsiSetRights opsiSetRightsCommand = new SingleCommandOpsiSetRights(dir);
		CommandExecutor executor = new CommandExecutor(configedMain, opsiSetRightsCommand);
		executor.execute();
	}

	private void execute() {
		if (jLabelProductVersionControlFile.getText() == null || jLabelProductVersionControlFile.getText().isEmpty()) {
			Logging.warning(this, "Please select a valid opsi product directory.");
			return;
		}
		MultiCommandTemplate commands = new MultiCommandTemplate();
		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		String dirLocationInServer = Utils.getServerPathFromWebDAVPath(dir);

		String prodVersion = jTextFieldProductVersion.getText();
		String packVersion = jTextFieldPackageVersion.getText();
		prodVersion = checkVersion(prodVersion, Configed.getResourceValue("MakeProductFileDialog.keepVersions"), "");
		packVersion = checkVersion(packVersion, Configed.getResourceValue("MakeProductFileDialog.keepVersions"), "");
		SingleCommandOpsiMakeProductFile opsiMakeProductFileCommand = new SingleCommandOpsiMakeProductFile(
				dirLocationInServer, packVersion, prodVersion, advancedOptionsPanel.useMD5Sum(),
				advancedOptionsPanel.useZsync());
		commands.setMainName(opsiMakeProductFileCommand.getMenuText());
		if (jCheckBoxOverwrite.isSelected()) {
			String versions = doActionGetVersions();

			String[] versionArray = tripleSemicolonMatcher.split(versions);

			prodVersion = checkVersion(prodVersion, "", versionArray[1]);
			packVersion = checkVersion(packVersion, "", versionArray[0]);
			String packageID = getPackageID(dirLocationInServer);
			filename = dir + "" + packageID + "_" + prodVersion + "-" + packVersion + ".opsi";
			String serverPath = dirLocationInServer + "" + packageID + "_" + prodVersion + "-" + packVersion + ".opsi";

			String command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, serverPath);

			SingleCommandTemplate removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, serverPath + ".zsync");

			removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, serverPath + ".md5");
			removeExistingPackage = new SingleCommandTemplate(command);

			commands.addCommand(removeExistingPackage);
		}
		if (advancedOptionsPanel.setRights()) {
			commands.addCommand(new SingleCommandOpsiSetRights(dirLocationInServer));
		}
		commands.addCommand(opsiMakeProductFileCommand);

		Logging.info(this, "Start Commands ", commands);
		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain, commands);
				executor.execute();
			}
		}.start();
	}

	private static String checkVersion(String v, String compareWith, String overwriteWith) {
		if (v.equals(compareWith)) {
			return overwriteWith;
		}

		return v;
	}

	private String getPackageID(String dir) {
		SingleCommandTemplate getPackageId = new SingleCommandTemplate(
				GET_PACKAGE_ID_COMMAND.replace(DIRECTORY_REPLACEMENT_PATTERN, dir));
		CommandExecutor executor = new CommandExecutor(configedMain, getPackageId);
		executor.setWithGUI(false);
		String result = executor.execute();
		Logging.debug(this, "getPackageID result ", result);
		return result != null ? result.replace("id:", "").trim() : "";
	}
}
