/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandFileUpload;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.share.FileUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PackageManagerInstallParameterDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel downloadPanel = new JPanel();
	private PMInstallLocalPanel installLocalPanel;
	private PMInstallServerPanel installServerPanel;
	private PMInstallCurlPanel installCurlPanel;
	private PMInstallSettingsPanel installSettingsPanel;

	private JComboBox<String> jComboBoxPackageSource;

	private String fromMakeProductfile;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public PackageManagerInstallParameterDialog(ConfigedMain configedMain) {
		this(configedMain, "");
	}

	public PackageManagerInstallParameterDialog(ConfigedMain configedMain, String fullPathToPackage) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		fromMakeProductfile = fullPathToPackage;

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JOptionPane optionPane = new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { buttonExecute, Configed.getResourceValue("buttonCancel") });
		DialogUtils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageManagerInstallParameterDialog.title"));
		dialog.setModal(false);

		// Create the panel after the dialog has been created, because 
		// we need the dialog for the panel
		initInstances();
		initLayout();

		// We need to pack the dialog because the size of the mainPanel has changed
		dialog.pack();

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());

		// the dialog should resize to the size of the content
		installCurlPanel.addDialogToReactOn(dialog);

		dialog.setVisible(true);
	}

	private void initInstances() {
		jComboBoxPackageSource = new JComboBox<>(
				new String[] { Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromLocal"),
						Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromServer"),
						Configed.getResourceValue(
								"PackageManagerInstallParameterDialog.opsipackagemanager_install.jLabelCurlFrom") });

		jComboBoxPackageSource.addItemListener(itemEvent -> packageSourceChanged());

		installLocalPanel = new PMInstallLocalPanel();
		installServerPanel = new PMInstallServerPanel(fromMakeProductfile);
		installCurlPanel = new PMInstallCurlPanel();

		installSettingsPanel = new PMInstallSettingsPanel(dialog);

		if (fromMakeProductfile != null && !fromMakeProductfile.isEmpty()) {
			jComboBoxPackageSource.setSelectedItem(
					Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromServer"));

			installLocalPanel.isOpen(true);
			installLocalPanel.close();

			installServerPanel.isOpen(false);
			installServerPanel.open();

			installServerPanel.setPackagePath(fromMakeProductfile);
		} else {
			jComboBoxPackageSource
					.setSelectedItem(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromLocal"));

			installLocalPanel.isOpen(false);
			installLocalPanel.open();

			installServerPanel.isOpen(true);
			installServerPanel.close();
		}

		installCurlPanel.isOpen(true);
		installCurlPanel.close();
	}

	private void packageSourceChanged() {
		if (jComboBoxPackageSource.getSelectedItem()
				.equals(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromLocal"))) {
			installLocalPanel.open();
			installServerPanel.close();
			installCurlPanel.close();
		} else if (jComboBoxPackageSource.getSelectedItem()
				.equals(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromServer"))) {
			installLocalPanel.close();
			installServerPanel.open();
			installCurlPanel.close();
		} else if (jComboBoxPackageSource.getSelectedItem().equals(Configed
				.getResourceValue("PackageManagerInstallParameterDialog.opsipackagemanager_install.jLabelCurlFrom"))) {
			installLocalPanel.close();
			installServerPanel.close();
			installCurlPanel.open();
		} else {
			Logging.warning(this, "Unknown source selected");
		}

		if (dialog != null) {
			dialog.pack();
		}
	}

	private void initLayout() {
		downloadPanel.setBorder(BorderFactory.createTitledBorder(""));
		JLabel jLabelInstall = SwingUtils
				.createBoldLabel("PackageManagerInstallParameterDialog.jLabelInstallationMethod");

		downloadPanel.setLayout(
				new MigLayout("insets " + Globals.GAP_SIZE + ", fillx, gapy " + Globals.GAP_SIZE + ", wrap 1",
						"[grow, fill]", "[]0[]0[]"));

		downloadPanel.add(installLocalPanel, "growx, hidemode 3");
		downloadPanel.add(installServerPanel, "growx, hidemode 3");
		downloadPanel.add(installCurlPanel, "growx, hidemode 3");

		mainPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.MIN_GAP_SIZE + ", wrap 1", "[grow, fill]",
				"[]0[]0[]0[]"));

		mainPanel.add(jLabelInstall, "gapbottom " + Globals.MIN_GAP_SIZE);
		mainPanel.add(jComboBoxPackageSource, "gapbottom " + Globals.MIN_GAP_SIZE);
		mainPanel.add(downloadPanel, "growx, gapbottom " + Globals.GAP_SIZE);
		mainPanel.add(installSettingsPanel, "growx");

	}

	private void execute() {
		Logging.info(this, " doAction3 install ");
		MultiCommandTemplate commands = new MultiCommandTemplate();
		commands.setMainName("PackageInstallation");
		SingleCommandOpsiPackageManagerInstall pmInstallCom;

		if (jComboBoxPackageSource.getSelectedItem()
				.equals(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromLocal"))) {
			pmInstallCom = handleLocalFileUpload(commands);
		} else if (jComboBoxPackageSource.getSelectedItem()
				.equals(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromServer"))) {
			pmInstallCom = handleServerFileUpload(commands);
		} else {
			pmInstallCom = handleCurlFileUpload(commands);
		}

		if (pmInstallCom == null) {
			return;
		}

		installSettingsPanel.updateCommand(pmInstallCom);

		// Execute the command
		new CommandExecutor(configedMain, commands).execute();

		Logging.info(this, "doAction3 end ");
	}

	private SingleCommandOpsiPackageManagerInstall handleLocalFileUpload(MultiCommandTemplate commands) {
		SingleCommandFileUpload fileUploadCommand = installLocalPanel.getCommand();
		SingleCommandOpsiPackageManagerInstall pmInstallCom = null;
		if (fileUploadCommand == null) {
			Logging.warning(this, "No opsi-package given. 1");
			return pmInstallCom;
		}
		commands.addCommand(fileUploadCommand);
		pmInstallCom = PMInstallServerPanel
				.getCommand(FileUtils.getServerPathFromWebDAVPath(fileUploadCommand.getFullTargetPath()));
		if (pmInstallCom == null) {
			Logging.warning(this, "No url given. 2");
			Logging.warning(this, "ERROR 0 command = null");
		} else {
			commands.addCommand(pmInstallCom);
		}
		return pmInstallCom;
	}

	private SingleCommandOpsiPackageManagerInstall handleServerFileUpload(MultiCommandTemplate commands) {
		SingleCommandOpsiPackageManagerInstall pmInstallCom = installServerPanel.getCommand();
		if (pmInstallCom == null) {
			Logging.warning(this, "No opsi-package selected. 3");
		} else {
			commands.addCommand(pmInstallCom);
		}
		return pmInstallCom;
	}

	private SingleCommandOpsiPackageManagerInstall handleCurlFileUpload(MultiCommandTemplate commands) {
		SingleCommandOpsiPackageManagerInstall pmInstallCom = null;
		commands = installCurlPanel.getCommand(commands);
		if (commands == null) {
			Logging.warning(this, "No opsi-package given.4");
			return pmInstallCom;
		}

		pmInstallCom = PMInstallServerPanel.getCommand(installCurlPanel.getProduct());
		Logging.info(this, "c ", pmInstallCom);
		if (pmInstallCom != null) {
			commands.addCommand(pmInstallCom);
		} else {
			Logging.warning(this, "ERROR 3 command = null");
		}
		return pmInstallCom;
	}
}
