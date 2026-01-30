/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
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
import net.miginfocom.swing.MigLayout;

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
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
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
		buttonExecute.addActionListener(actionEvent -> new Thread(this::execute));

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
		jLabelDir = Utils.createBoldLabel("MakeProductFileDialog.serverDir");

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
		jButtonSearchDir.addActionListener(actionEvent -> search());

		jLabelPackageVersion = Utils.createBoldLabel("MakeProductFileDialog.packageVersion");
		jLabelProductVersion = Utils.createBoldLabel("MakeProductFileDialog.productVersion");
		jLabelVersionsControlFile = Utils.createBoldLabel("MakeProductFileDialog.versions_controlfile");
		jLabelVersions = Utils.createBoldLabel("MakeProductFileDialog.versions");

		jLabelProductVersionControlFile = new JLabel();
		jLabelPackageVersionControlFile = new JLabel();
		jTextFieldPackageVersion = new JTextField();

		jTextFieldProductVersion = new JTextField();

		enableTfVersions(false);

		jCheckBoxOverwrite = new JCheckBox(Configed.getResourceValue("MakeProductFileDialog.removeExisting"), true);

		jButtonAdvancedSettings = new JToggleButton(
				Configed.getResourceValue("MakeProductFileDialog.btn_advancedSettings"));

		jButtonAdvancedSettings.addActionListener(actionEvent -> toggleAdvancedSettings());

		jButtonSetRights = new JButton(Configed.getResourceValue("MakeProductFileDialog.btn_setRights"));
		jButtonSetRights.setToolTipText(Configed.getResourceValue("MakeProductFileDialog.btn_setRights.tooltip"));
		jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 0, wrap 1, hidemode 2", "[grow]", "[]0"));

		panel.add(jLabelDir, "gapbottom " + Globals.GAP_SIZE);

		panel.add(jComboBoxMainDir, "split 2, growx, pushx, wmin 0, wmax pref");
		panel.add(jButtonSearchDir, "gapleft " + Globals.GAP_SIZE + ", wrap");

		panel.add(jButtonSetRights, "gaptop " + Globals.GAP_SIZE);

		JPanel versionPanel = initVersionPanel();
		panel.add(versionPanel, "span, growx, pushx, gaptop " + Globals.GAP_SIZE);

		panel.add(jCheckBoxOverwrite, "gaptop " + Globals.GAP_SIZE);
		panel.add(jButtonAdvancedSettings);

		panel.add(advancedOptionsPanel, "gaptop " + Globals.GAP_SIZE);

		return panel;
	}

	private JPanel initVersionPanel() {
		JPanel versionPanel = new JPanel(new MigLayout("insets 0, hidemode 2, wrap 1", "[][pref!][grow][]"));

		versionPanel.add(jLabelVersionsControlFile, "cell 1 0, align right");
		versionPanel.add(jLabelVersions, "cell 3 0, align left");

		versionPanel.add(jLabelProductVersion, "cell 0 1, align right");
		versionPanel.add(jLabelProductVersionControlFile, "cell 1 1");
		versionPanel.add(jTextFieldProductVersion, "cell 3 1");

		versionPanel.add(jLabelPackageVersion, "cell 0 2, align right");
		versionPanel.add(jLabelPackageVersionControlFile, "cell 1 2");
		versionPanel.add(jTextFieldPackageVersion, "cell 3 2");

		return versionPanel;
	}

	private void search() {
		autocompletion.doButtonAction();
		doSetActionGetVersions();
	}

	private void toggleAdvancedSettings() {
		advancedOptionsPanel.setVisible(!advancedOptionsPanel.isVisible());
		dialog.pack();
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
			Logging.info(this, "doActionGetVersions, getDirectories result versions with length ", versions.length);
			if (versions.length < 2) {
				Logging.info(this, "doActionGetVersions, not expected versions array with size < 2");
				return "";
			}
			return versions[0] + ";;;" + versions[1];
		}
		return "";
	}

	private final void doSetActionGetVersions() {
		Utils.runSwingWorker(this::doActionGetVersions, this::setVersions, null);
	}

	private void setVersions(String versions) {
		if (versions.contains(";;;")) {
			enableTfVersions(true);

			String[] versionArray = tripleSemicolonMatcher.split(versions, 2);
			updateVersionFields(versionArray[0], versionArray[1]);
		} else {
			enableTfVersions(false);
			updateVersionFields("", "");
		}
	}

	private void updateVersionFields(String product, String packageVersion) {
		jTextFieldPackageVersion.setText(product);
		jLabelPackageVersionControlFile.setText(product);

		jTextFieldProductVersion.setText(packageVersion);
		jLabelProductVersionControlFile.setText(packageVersion);
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

			String[] versionArray = tripleSemicolonMatcher.split(versions, 2);

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
