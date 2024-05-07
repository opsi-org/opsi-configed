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
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandOpsiMakeProductFile;
import de.uib.configed.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.serverconsole.command.SingleCommandTemplate;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class MakeProductFileDialog extends FGeneralDialog {
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

	private JButton jButtonToPackageManager;
	private JButton jButtonExec;
	private String filename;
	private ConfigedMain configedMain;
	private CompletionComboButton autocompletion;

	public MakeProductFileDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("MakeProductFileDialog.title"), false);
		this.configedMain = configedMain;
		autocompletion = new CompletionComboButton();
		advancedOptionsPanel = new AdvancedOptionsPanel();
		initGUI();

		filename = "";

		advancedOptionsPanel.showAdvancedSettings();
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
			jTextFieldPackageVersion.setEnabled(value);
			jTextFieldProductVersion.setEnabled(value);
		}
		jComboBoxMainDir.setEnabled(value);
		jCheckBoxOverwrite.setEnabled(value);
	}

	private String setOpsiPackageFilename(String path) {
		filename = path;
		jButtonToPackageManager.setEnabled(true);
		jButtonToPackageManager.setToolTipText(
				Configed.getResourceValue("MakeProductFileDialog.buttonToPackageManager.tooltip") + " " + filename);
		return filename;
	}

	private void initGUI() {
		JPanel workbenchPanel = new JPanel();
		JPanel mainButtonPanel = new JPanel();

		mainButtonPanel.setLayout(new BorderLayout());
		mainButtonPanel.add(advancedOptionsPanel, BorderLayout.NORTH);
		mainButtonPanel.add(initButtonPanel(), BorderLayout.SOUTH);

		getContentPane().add(workbenchPanel, BorderLayout.CENTER);
		getContentPane().add(mainButtonPanel, BorderLayout.SOUTH);

		GroupLayout workbenchPanelLayout = new GroupLayout(workbenchPanel);
		workbenchPanelLayout.setAutoCreateGaps(true);
		workbenchPanelLayout.setAutoCreateContainerGaps(true);
		workbenchPanel.setLayout(workbenchPanelLayout);

		workbenchPanel.setBorder(BorderFactory.createTitledBorder(""));

		JLabel jLabelDir = new JLabel(Configed.getResourceValue("MakeProductFileDialog.serverDir"));

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
				"    " + Configed.getResourceValue("MakeProductFileDialog.packageVersion"));
		JLabel jLabelProductVersion = new JLabel(
				"    " + Configed.getResourceValue("MakeProductFileDialog.productVersion"));
		JLabel jLabelVersionsControlFile = new JLabel(
				Configed.getResourceValue("MakeProductFileDialog.versions_controlfile"));
		JLabel jLabelVersions = new JLabel(Configed.getResourceValue("MakeProductFileDialog.versions"));
		JLabel jLabelSetRightsNow = new JLabel(Configed.getResourceValue("MakeProductFileDialog.setRights_now"));
		JLabel jLabelRemoveExistingPackage = new JLabel(
				Configed.getResourceValue("MakeProductFileDialog.removeExisting"));

		jLabelProductVersionControlFile = new JLabel();
		jLabelPackageVersionControlFile = new JLabel();
		jTextFieldPackageVersion = new JTextField();

		jTextFieldProductVersion = new JTextField();

		enableTfVersions(false);

		jCheckBoxOverwrite = new JCheckBox();
		jCheckBoxOverwrite.setSelected(true);

		JButton jButtonAdvancedSettings = new JButton(
				Configed.getResourceValue("MakeProductFileDialog.btn_advancedSettings"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonAdvancedSettings.addActionListener(actionEvent -> advancedOptionsPanel.showAdvancedSettings());
		}

		jTextFieldProductVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());
		jTextFieldPackageVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());

		JButton jButtonSetRights = new JButton(Configed.getResourceValue("MakeProductFileDialog.btn_setRights"));
		jButtonSetRights.setToolTipText(Configed.getResourceValue("MakeProductFileDialog.btn_setRights.tooltip"));
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());
		}

		workbenchPanelLayout
				.setHorizontalGroup(workbenchPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
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
						.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
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

								.addGroup(workbenchPanelLayout.createSequentialGroup().addComponent(jCheckBoxOverwrite,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)))
						.addGap(Globals.GAP_SIZE)
						.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldPackageVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonAdvancedSettings, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		workbenchPanelLayout.setVerticalGroup(workbenchPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxMainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelSetRightsNow, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonSetRights, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPackageVersionControlFile, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldPackageVersion, GroupLayout.Alignment.LEADING,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelRemoveExistingPackage, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(workbenchPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						jButtonAdvancedSettings, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
	}

	private JPanel initButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

		jButtonToPackageManager = new JButton(
				Configed.getResourceValue("MakeProductFileDialog.buttonToPackageManager"));
		jButtonToPackageManager.setEnabled(false);
		jButtonToPackageManager
				.setToolTipText(Configed.getResourceValue("MakeProductFileDialog.buttonToPackageManager.tooltip"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonToPackageManager.addActionListener((ActionEvent actionEvent) -> {
				if (configedMain != null) {
					new PackageManagerInstallParameterDialog(configedMain, filename);
				}
			});
		}

		jButtonExec = new JButton(Configed.getResourceValue("buttonExecute"));

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

		return buttonPanel;
	}

	private String doActionGetVersions() {
		String dir = Utils.getServerPathFromWebDAVPath((String) jComboBoxMainDir.getEditor().getItem())
				+ "OPSI/control";
		Logging.info(this, "doActionGetVersions, dir " + dir);
		SingleCommandTemplate getVersions = new SingleCommandTemplate(
				GET_VERSIONS_COMMAND.replace(DIRECTORY_REPLACEMENT_PATTERN, dir));
		CommandExecutor executor = new CommandExecutor(configedMain, getVersions);
		executor.setWithGUI(false);
		Logging.info(this, "doActionGetVersions, command " + getVersions);
		String result = executor.execute();
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

			jTextFieldPackageVersion.setText(versionArray[0]);
			jLabelPackageVersionControlFile.setText(versionArray[0]);

			jTextFieldProductVersion.setText(versionArray[1]);
			jLabelProductVersionControlFile.setText(versionArray[1]);

			jButtonExec.setEnabled(true);
		} else {
			enableTfVersions(false);
			jTextFieldPackageVersion.setText("");
			jLabelPackageVersionControlFile.setText("");
			jTextFieldProductVersion.setText("");
			jLabelProductVersionControlFile.setText("");

			jButtonExec.setEnabled(false);
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
			setOpsiPackageFilename(dir + "" + packageID + "_" + prodVersion + "-" + packVersion + ".opsi");
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

		Logging.info(this, "Start Commands " + commands);
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
		Logging.debug(this, "getPackageID result " + result);
		return result != null ? result.replace("id:", "").trim() : "";
	}
}
