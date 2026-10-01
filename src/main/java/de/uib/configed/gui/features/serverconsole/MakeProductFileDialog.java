/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.util.regex.Matcher;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiMakeProductFile;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandTemplate;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.swing.AutoCompletionComboBox;
import de.uib.configed.share.FileUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class MakeProductFileDialog {
	private static final Pattern tripleSemicolonMatcher = Pattern.compile(";;;");
	private static final String FILE_REPLACEMENT_PATTERN = "*.file.*";
	private static final String REMOVE_EXISTING_FILE_COMMAND = "[ -f " + FILE_REPLACEMENT_PATTERN + " ] &&  rm "
			+ FILE_REPLACEMENT_PATTERN + " && echo \"File " + FILE_REPLACEMENT_PATTERN + " removed\" || echo \"File "
			+ FILE_REPLACEMENT_PATTERN + " does not exist\"";
	private static final String DIRECTORY_REPLACEMENT_PATTERN = "*.dir.*";
	// Prefers OPSI/control.toml (version = "x") over the legacy OPSI/control (version: x) format.
	private static final String GET_VERSIONS_COMMAND = "[ -f " + DIRECTORY_REPLACEMENT_PATTERN
			+ "OPSI/control.toml ] && grep -oE 'version[[:space:]]*=[[:space:]]*\"[^\"]*\"' "
			+ DIRECTORY_REPLACEMENT_PATTERN + "OPSI/control.toml --max-count=2 || grep version: "
			+ DIRECTORY_REPLACEMENT_PATTERN + "OPSI/control --max-count=2";
	private static final Pattern VERSION_LINE_PATTERN = Pattern
			.compile("version\\s*[:=]\\s*\"?([^\"\\r\\n]+?)\"?\\s*$", Pattern.UNICODE_CHARACTER_CLASS);
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
	private JButton buttonExecute;
	private JButton buttonPackageManager;
	private JLabel jLabelProductVersion;
	private JLabel jLabelPackageVersion;
	private JLabel jLabelVersionsControlFile;
	private JLabel jLabelVersions;
	private JToggleButton jButtonAdvancedSettings;

	private String localPackagePath;
	private ConfigedMain configedMain;
	private CompletionComboButton autocompletion;

	private JDialog dialog;

	// Prevents overlapping CommandExecutor runs from rapid combo box changes or search clicks.
	private volatile boolean versionLookupInProgress;

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

		localPackagePath = "";

		jComboBoxMainDir.setEnabled(true);

		buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());
		buttonExecute.setEnabled(false);

		buttonPackageManager = new JButton(Configed.getResourceValue("MakeProductFileDialog.buttonToPackageManager"));
		buttonPackageManager.addActionListener(
				actionEvent -> new PackageManagerInstallParameterDialog(configedMain, localPackagePath));
		buttonPackageManager.setEnabled(false);

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
				null, new Object[] { buttonExecute, buttonPackageManager, Configed.getResourceValue("buttonCancel") });
		DialogUtils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MakeProductFileDialog.title"));
		dialog.setModal(false);
		dialog.pack();

		dialog.setVisible(true);

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void initComponents() {
		jLabelDir = SwingUtils.createBoldLabel("MakeProductFileDialog.serverDir");

		autocompletion.setCombobox(new AutoCompletionComboBox<>(
				new DefaultComboBoxModel<>(autocompletion.getDefaultValues().toArray(new String[0]))) {
			@Override
			public void setSelectedItem(Object item) {
				super.setSelectedItem(item);
				buttonPackageManager.setEnabled(false);
				doSetActionGetVersions();
			}
		});
		autocompletion.initCombobox();
		jComboBoxMainDir = autocompletion.getCombobox();

		jButtonSearchDir = autocompletion.getButton();
		jButtonSearchDir.removeActionListener(jButtonSearchDir.getActionListeners()[0]);
		jButtonSearchDir.addActionListener(actionEvent -> search());

		jLabelPackageVersion = SwingUtils.createBoldLabel("MakeProductFileDialog.packageVersion");
		jLabelProductVersion = SwingUtils.createBoldLabel("MakeProductFileDialog.productVersion");
		jLabelVersionsControlFile = SwingUtils.createBoldLabel("MakeProductFileDialog.versions_controlfile");
		jLabelVersions = SwingUtils.createBoldLabel("MakeProductFileDialog.versions");

		jLabelProductVersionControlFile = new JLabel();
		jLabelPackageVersionControlFile = new JLabel();
		jTextFieldPackageVersion = new JTextField();
		jTextFieldPackageVersion.getDocument()
				.addDocumentListener(new VersionDocumentListener(jTextFieldPackageVersion));

		jTextFieldProductVersion = new JTextField();
		jTextFieldProductVersion.getDocument()
				.addDocumentListener(new VersionDocumentListener(jTextFieldProductVersion));

		enableTfVersions(false);

		jCheckBoxOverwrite = new JCheckBox(Configed.getResourceValue("MakeProductFileDialog.removeExisting"), true);

		jButtonAdvancedSettings = new JToggleButton(
				Configed.getResourceValue("MakeProductFileDialog.btn_advancedSettings"));

		jButtonAdvancedSettings.addActionListener(actionEvent -> toggleAdvancedSettings());

		jButtonSetRights = new JButton(Configed.getResourceValue("MakeProductFileDialog.btn_setRights"));
		jButtonSetRights.setToolTipText(Configed.getResourceValue("MakeProductFileDialog.btn_setRights.tooltip"));
		jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());
	}

	private class VersionDocumentListener implements DocumentListener {
		private JTextField textField;

		public VersionDocumentListener(JTextField textField) {
			this.textField = textField;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateButton();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateButton();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateButton();
		}

		private void updateButton() {
			boolean hasContent = !textField.getText().trim().isEmpty();
			buttonExecute.setEnabled(hasContent);
		}
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
	}

	private void toggleAdvancedSettings() {
		advancedOptionsPanel.setVisible(!advancedOptionsPanel.isVisible());
		dialog.pack();
	}

	private String doActionGetVersions(String dir) {
		String dirLocationInServer = FileUtils.getServerPathFromWebDAVPath(dir);
		Logging.info(this, "doActionGetVersions, dir ", dirLocationInServer);
		SingleCommandTemplate getVersions = new SingleCommandTemplate(
				GET_VERSIONS_COMMAND.replace(DIRECTORY_REPLACEMENT_PATTERN, dirLocationInServer));
		CommandExecutor executor = new CommandExecutor(configedMain, getVersions);
		executor.setWithGUI(false);
		Logging.info(this, "doActionGetVersions, command ", getVersions);
		String result = executor.execute();
		Logging.info(this, "doActionGetVersions result ", result);

		if (result == null || result.isEmpty()) {
			Logging.warning(this, "doActionGetVersions, could not find versions in ", dirLocationInServer,
					".Please check if directory exists and contains the file OPSI/control.toml or OPSI/control.\n",
					"Please also check the rights of the file/s.");
		} else {
			String[] versions = result.split("\n");
			Logging.info(this, "doActionGetVersions, getDirectories result versions with length ", versions.length);
			if (versions.length < 2) {
				Logging.info(this, "doActionGetVersions, not expected versions array with size < 2");
				return "";
			}
			return extractVersionValue(versions[0]) + ";;;" + extractVersionValue(versions[1]);
		}
		return "";
	}

	private static String extractVersionValue(String line) {
		Matcher matcher = VERSION_LINE_PATTERN.matcher(line);
		return matcher.find() ? matcher.group(1).trim() : "";
	}

	private final void doSetActionGetVersions() {
		if (versionLookupInProgress) {
			return;
		}
		versionLookupInProgress = true;
		jButtonSearchDir.setEnabled(false);
		jComboBoxMainDir.setEnabled(false);

		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		SwingUtils.runSwingWorker(() -> doActionGetVersions(dir), this::onVersionLookupDone,
				exception -> onVersionLookupDone(""));
	}

	private void onVersionLookupDone(String versions) {
		setVersions(versions);
		jButtonSearchDir.setEnabled(true);
		jComboBoxMainDir.setEnabled(true);
		versionLookupInProgress = false;
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
		executor.executeAsync();
	}

	private void execute() {
		if (jLabelProductVersionControlFile.getText() == null || jLabelProductVersionControlFile.getText().isEmpty()) {
			Logging.warning(this, "Please select a valid opsi product directory.");
			return;
		}
		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		String prodVersion = checkVersion(jTextFieldProductVersion.getText(),
				Configed.getResourceValue("MakeProductFileDialog.keepVersions"), "");
		String packVersion = checkVersion(jTextFieldPackageVersion.getText(),
				Configed.getResourceValue("MakeProductFileDialog.keepVersions"), "");

		boolean overwrite = jCheckBoxOverwrite.isSelected();
		boolean setRights = advancedOptionsPanel.setRights();

		SwingUtils.runSwingWorker(() -> getCommands(dir, prodVersion, packVersion, overwrite, setRights),
				(MultiCommandTemplate commands) -> {
					Logging.info(this, "Start Commands ", commands);
					CommandExecutor executor = new CommandExecutor(configedMain, commands);
					executor.setWithGUI(true);
					executor.executeAsync();
				}, null);
	}

	private MultiCommandTemplate getCommands(String dir, String prodVersion, String packVersion, boolean overwrite,
			boolean setRights) {
		String dirLocationInServer = FileUtils.getServerPathFromWebDAVPath(dir);

		SingleCommandOpsiMakeProductFile opsiMakeProductFileCommand = new SingleCommandOpsiMakeProductFile(
				dirLocationInServer, packVersion, prodVersion, advancedOptionsPanel.useMD5Sum(),
				advancedOptionsPanel.useZsync());

		MultiCommandTemplate commands = new MultiCommandTemplate();
		commands.setMainName(opsiMakeProductFileCommand.getMenuText());

		if (overwrite) {
			String versions = doActionGetVersions(dir);

			String[] versionArray = tripleSemicolonMatcher.split(versions, 2);

			prodVersion = checkVersion(prodVersion, "", versionArray[1]);
			packVersion = checkVersion(packVersion, "", versionArray[0]);

			String packageID = getPackageID(dirLocationInServer);
			localPackagePath = dir + "" + packageID + "_" + prodVersion + "-" + packVersion + ".opsi";
			String packagePathInServer = dirLocationInServer + "" + packageID + "_" + prodVersion + "-" + packVersion
					+ ".opsi";
			buttonPackageManager.setEnabled(true);

			String command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, packagePathInServer);

			SingleCommandTemplate removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, packagePathInServer + ".zsync");

			removeExistingPackage = new SingleCommandTemplate(command);
			commands.addCommand(removeExistingPackage);

			command = REMOVE_EXISTING_FILE_COMMAND.replace(FILE_REPLACEMENT_PATTERN, packagePathInServer + ".md5");
			removeExistingPackage = new SingleCommandTemplate(command);

			commands.addCommand(removeExistingPackage);
		}

		if (setRights) {
			commands.addCommand(new SingleCommandOpsiSetRights(dirLocationInServer));
		}

		commands.addCommand(opsiMakeProductFileCommand);
		return commands;
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
