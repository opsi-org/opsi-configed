/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.CompletionComboBox;
import de.uib.configed.gui.ssh.SSHPackageManagerInstallParameterDialog;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandOpsiMakeProductFile;
import de.uib.configed.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.serverconsole.command.SingleCommandTemplate;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class MakeProductFileDialog extends FGeneralDialog {
	private static final Pattern tripleSemicolonMatcher = Pattern.compile(";;;");

	// In dieser Klasse gibt es Linux-Befehle (folgend), die zu Konstanten
	// ausgelagert werden sollen (noch nicht funktioniert)
	private JLabel jLabelProductVersionControlFile;
	private JLabel jLabelPackageVersionControlFile;
	private JTextField jTextFieldPckageVersion;
	private JTextField jTextFieldProductVersion;
	private JComboBox<String> jComboBoxMainDir;
	private JCheckBox jCheckBoxmd5sum;
	private JCheckBox jCheckBoxzsync;
	private JCheckBox jCheckBoxOverwrite;
	private JCheckBox jCheckBoxSetRights;
	private JPanel mainpanel;

	private JButton jButtonToPackageManager;
	private JButton jButtonExec;
	private String filename;
	private ConfigedMain configedMain;
	private boolean isAdvancedOpen = true;
	private CompletionComboButton autocompletion;

	public MakeProductFileDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"), false);
		this.configedMain = configedMain;
		autocompletion = new CompletionComboButton();
		initGUI();

		filename = "";

		autocompletion.doButtonAction();
		doSetActionGetVersions();
		showAdvancedSettings();
		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		initFrame();
	}

	private void initFrame() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(Globals.DIALOG_FRAME_DEFAULT_WIDTH + 100, Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100);
		setLocationRelativeTo(ConfigedMain.getMainFrame());
		setVisible(true);
	}

	private void setComponentsEnabled(boolean value) {
		jButtonExec.setEnabled(value);
		if (!value) {
			jTextFieldPckageVersion.setEnabled(value);
			jTextFieldProductVersion.setEnabled(value);
		}
		jComboBoxMainDir.setEnabled(value);
		jCheckBoxmd5sum.setEnabled(value);
		jCheckBoxzsync.setEnabled(value);
		jCheckBoxOverwrite.setEnabled(value);
	}

	private String setOpsiPackageFilename(String path) {
		filename = path;
		jButtonToPackageManager.setEnabled(true);
		jButtonToPackageManager.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip") + " " + filename);
		return filename;
	}

	private void initGUI() {
		JPanel workbenchpanel = new JPanel();
		mainpanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		JPanel mainButtonPanel = new JPanel();

		mainButtonPanel.setLayout(new BorderLayout());
		mainButtonPanel.add(mainpanel, BorderLayout.NORTH);
		mainButtonPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(workbenchpanel, BorderLayout.CENTER);
		getContentPane().add(mainButtonPanel, BorderLayout.SOUTH);

		GroupLayout mainpanelLayout = new GroupLayout(mainpanel);
		GroupLayout workbenchpanelLayout = new GroupLayout(workbenchpanel);
		workbenchpanel.setLayout(workbenchpanelLayout);
		mainpanel.setLayout(mainpanelLayout);

		workbenchpanel.setBorder(BorderFactory.createTitledBorder(""));
		mainpanel.setBorder(BorderFactory.createTitledBorder(""));
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

		JLabel jLabelDir = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.serverDir"));

		autocompletion.setCombobox(new CompletionComboBox<>(
				new DefaultComboBoxModel<>(autocompletion.getDefaultValues().toArray(new String[0]))) {
			@Override
			public void setSelectedItem(Object item) {
				super.setSelectedItem(item);
				doSetActionGetVersions();
			}
		});
		autocompletion.initCombobox();
		jComboBoxMainDir = autocompletion.getCombobox();

		JButton jButtonSearchDir = autocompletion.getButton();
		jButtonSearchDir.removeActionListener(jButtonSearchDir.getActionListeners()[0]);
		jButtonSearchDir.addActionListener((ActionEvent actionEvent) -> {
			autocompletion.doButtonAction();
			doSetActionGetVersions();
		});

		JLabel jLabelPackageVersion = new JLabel(
				"    " + Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageVersion"));

		JLabel jLabelProductVersion = new JLabel(
				"    " + Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.productVersion"));

		JLabel jLabelVersionsControlFile = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions_controlfile"));

		JLabel jLabelVersions = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions"));

		JLabel jLabelSetRights = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights"));

		JLabel jLabelSetRightsNow = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights_now"));

		JLabel jLabelRemoveExistingPackage = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting"));

		JLabel jLabelRemoveExistingPackage2 = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting2"));

		jLabelProductVersionControlFile = new JLabel();
		jLabelPackageVersionControlFile = new JLabel();
		jTextFieldPckageVersion = new JTextField();

		jTextFieldProductVersion = new JTextField();

		enableTfVersions(false);

		JLabel jLabelmd5sum = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createMd5sum"));

		jCheckBoxmd5sum = new JCheckBox();
		jCheckBoxmd5sum.setSelected(true);
		JLabel jLabelzsync = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createZsync"));

		jCheckBoxzsync = new JCheckBox();
		jCheckBoxzsync.setSelected(true);
		jCheckBoxOverwrite = new JCheckBox();
		jCheckBoxOverwrite.setSelected(true);
		jCheckBoxSetRights = new JCheckBox();
		jCheckBoxSetRights.setSelected(true);

		JButton jButtonAdvancedSettings = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_advancedSettings"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonAdvancedSettings.addActionListener(actionEvent -> showAdvancedSettings());
		}

		jButtonAdvancedSettings.setPreferredSize(jButtonSearchDir.getPreferredSize());
		jTextFieldProductVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());
		jTextFieldPckageVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());

		JButton jButtonSetRights = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights"));
		jButtonSetRights.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights.tooltip"));
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());
		}

		jButtonToPackageManager = new JButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager"));
		jButtonToPackageManager.setEnabled(false);
		jButtonToPackageManager.setToolTipText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonToPackageManager.addActionListener((ActionEvent actionEvent) -> {
				if (configedMain != null) {
					new SSHPackageManagerInstallParameterDialog(configedMain, filename);
				}
			});
		}

		jButtonExec = new JButton(Configed.getResourceValue("SSHConnection.buttonExec"));

		jButtonExec.setEnabled(false);
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonExec.addActionListener(actionEvent -> doAction2());
		}

		JButton jButtonCancel = new JButton(Configed.getResourceValue("buttonClose"));
		jButtonCancel.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonCancel);
		buttonPanel.add(jButtonToPackageManager);
		buttonPanel.add(jButtonExec);

		workbenchpanelLayout
				.setHorizontalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelSetRightsNow, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(
										jLabelRemoveExistingPackage, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jComboBoxMainDir, Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelProductVersionControlFile, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)
								.addComponent(jLabelPackageVersionControlFile, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)

								.addGroup(workbenchpanelLayout.createSequentialGroup()
										.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(jLabelRemoveExistingPackage2, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGap(Globals.GAP_SIZE)
						.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldPckageVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonAdvancedSettings, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		workbenchpanelLayout.setVerticalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxMainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelSetRightsNow, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSetRights, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPackageVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldPckageVersion, GroupLayout.Alignment.LEADING,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelRemoveExistingPackage, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelRemoveExistingPackage2, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						jButtonAdvancedSettings, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		mainpanelLayout.setHorizontalGroup(mainpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(mainpanelLayout.createParallelGroup()
						.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainpanelLayout.createParallelGroup()
						.addComponent(jCheckBoxzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));
		mainpanelLayout.setVerticalGroup(mainpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(mainpanelLayout.createParallelGroup()
						.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainpanelLayout.createParallelGroup()
						.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainpanelLayout.createParallelGroup()
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));
	}

	private void showAdvancedSettings() {
		if (isAdvancedOpen) {
			isAdvancedOpen = false;
			this.setSize(this.getWidth(), this.getHeight() - mainpanel.getHeight());
			mainpanel.setVisible(isAdvancedOpen);
		} else {
			isAdvancedOpen = true;
			this.setSize(this.getWidth(), this.getHeight() + mainpanel.getHeight());
			mainpanel.setVisible(isAdvancedOpen);
		}
	}

	private String doActionGetVersions() {
		String dir = jComboBoxMainDir.getEditor().getItem() + "/OPSI/control";
		Logging.info(this, "doActionGetVersions, dir " + dir);
		SingleCommandTemplate getVersions = new SingleCommandTemplate(
				CommandFactory.STRING_COMMAND_GET_VERSIONS.replace(CommandFactory.STRING_REPLACEMENT_DIRECTORY, dir));
		CommandExecutor executor = new CommandExecutor(configedMain, false);
		Logging.info(this, "doActionGetVersions, command " + getVersions);
		String result = executor.executeSingleCommand(getVersions);
		Logging.info(this, "doActionGetVersions result " + result);

		if (result == null || result.isEmpty()) {
			Logging.warning(this,
					"doActionGetVersions, could not find versions in file " + dir
							+ ".Please check if directory exists and contains the file OPSI/control.\n"
							+ "Please also check the rights of the file/s.");
		} else {
			String[] versions = result.replace("version: ", "").split("\n");
			Logging.info(this, "doActionGetVersions, getDirectories result " + Arrays.toString(versions));
			if (versions.length < 1) {
				Logging.info(this, "doActionGetVersions, not expected versions array " + Arrays.toString(versions));
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

			jTextFieldPckageVersion.setText(versionArray[0]);
			jLabelPackageVersionControlFile.setText(versionArray[0]);

			jTextFieldProductVersion.setText(versionArray[1]);
			jLabelProductVersionControlFile.setText(versionArray[1]);

			jButtonExec.setEnabled(true);
		} else {
			enableTfVersions(false);
			jTextFieldPckageVersion.setText("");
			jLabelPackageVersionControlFile.setText("");
			jTextFieldProductVersion.setText("");
			jLabelProductVersionControlFile.setText("");

			jButtonExec.setEnabled(false);
		}
	}

	private void enableTfVersions(boolean enable) {
		jTextFieldPckageVersion.setEnabled(enable);
		jTextFieldProductVersion.setEnabled(enable);
	}

	private void doExecSetRights() {
		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		SingleCommandOpsiSetRights opsiSetRightsCommand = new SingleCommandOpsiSetRights(dir);
		CommandExecutor executor = new CommandExecutor(configedMain);
		executor.executeSingleCommand(opsiSetRightsCommand);
	}

	private void cancel() {
		super.doAction1();
	}

	@Override
	public void doAction2() {
		if (jLabelProductVersionControlFile.getText() == null || jLabelProductVersionControlFile.getText().isEmpty()) {
			Logging.warning(this, "Please select a valid opsi product directory.");
			return;
		}
		MultiCommandTemplate commands = new MultiCommandTemplate();
		String dir = (String) jComboBoxMainDir.getEditor().getItem();

		String prodVersion = jTextFieldProductVersion.getText();
		String packVersion = jTextFieldPckageVersion.getText();
		prodVersion = checkVersion(prodVersion,
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		packVersion = checkVersion(packVersion,
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		SingleCommandOpsiMakeProductFile opsiMakeProductFileCommand = new SingleCommandOpsiMakeProductFile(dir,
				packVersion, prodVersion, jCheckBoxmd5sum.isSelected(), jCheckBoxzsync.isSelected());
		commands.setMainName(opsiMakeProductFileCommand.getMenuText());
		if (jCheckBoxOverwrite.isSelected()) {
			String versions = doActionGetVersions();

			String[] versionArray = tripleSemicolonMatcher.split(versions);

			prodVersion = checkVersion(prodVersion, "", versionArray[1]);
			packVersion = checkVersion(packVersion, "", versionArray[0]);
			setOpsiPackageFilename(dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi");

			String command = "[ -f " + filename + " ] &&  rm " + filename + " && echo \"File " + filename
					+ " removed\" || echo \"File did not exist\"";

			SingleCommandTemplate removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".zsync ] &&  rm " + filename + ".zsync && echo \"File " + filename
					+ ".zsync removed\" || echo \"File  " + filename + ".zsync did not exist\"";

			removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".md5 ] &&  rm " + filename + ".md5 && echo \"File " + filename
					+ ".md5 removed\" || echo \"File  " + filename + ".md5 did not exist\"";
			removeExistingPackage = new SingleCommandTemplate(command);

			commands.addCommand(removeExistingPackage);
		}
		if (jCheckBoxSetRights.isSelected()) {
			commands.addCommand(new SingleCommandOpsiSetRights(dir));
		}
		commands.addCommand(opsiMakeProductFileCommand);

		Logging.info(this, "SSHConnectExec " + commands);
		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain);
				executor.executeMultiCommand(commands);
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
				CommandFactory.STRING_COMMAND_CAT_DIRECTORY.replace(CommandFactory.STRING_REPLACEMENT_DIRECTORY, dir));
		CommandExecutor executor = new CommandExecutor(configedMain, false);
		String result = executor.executeSingleCommand(getPackageId);
		Logging.debug(this, "getPackageID result " + result);
		return result != null ? result.replace("id:", "").trim() : "";
	}
}
