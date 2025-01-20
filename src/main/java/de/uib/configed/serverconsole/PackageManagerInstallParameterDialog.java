/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandFileUpload;
import de.uib.configed.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class PackageManagerInstallParameterDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel downloadPanel = new JPanel();
	private PMInstallLocalPanel installLocalPanel;
	private PMInstallServerPanel installServerPanel;
	private PMInstallCurlPanel installCurlPanel;
	private PMInstallSettingsPanel installSettingsPanel;
	private JLabel jLabelInstall = new JLabel();

	private JComboBox<String> jComboBoxPackageSource;

	private String fromMakeProductfile;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public PackageManagerInstallParameterDialog(ConfigedMain configedMain) {
		this(configedMain, "");
	}

	public PackageManagerInstallParameterDialog(ConfigedMain configedMain, String fullPathToPackage) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		fromMakeProductfile = fullPathToPackage;

		initInstances();
		initLayout();

		JOptionPane optionPane = new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null,
				new Object[] { Configed.getResourceValue("buttonExecute"), Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("PackageManagerInstallParameterDialog.title"));

		// the dialog should resize to the size of the content
		installCurlPanel.addDialogToReactOn(dialog);

		dialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("buttonExecute"))) {
			execute();
		}
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

		dialog.pack();
	}

	private void initLayout() {
		downloadPanel.setBorder(BorderFactory.createTitledBorder(""));
		jLabelInstall.setText(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelInstall"));
		jLabelInstall.setFont(jLabelInstall.getFont().deriveFont(Font.BOLD));

		GroupLayout downloadPanelLayout = new GroupLayout(downloadPanel);
		downloadPanel.setLayout(downloadPanelLayout);

		downloadPanelLayout.setHorizontalGroup(downloadPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(downloadPanelLayout.createParallelGroup()
						.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(installCurlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		downloadPanelLayout.setVerticalGroup(downloadPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installCurlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup()
				.addComponent(jLabelInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxPackageSource, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(downloadPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(installSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		mainPanelLayout
				.setVerticalGroup(
						mainPanelLayout.createSequentialGroup().addComponent(jLabelInstall).addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jComboBoxPackageSource, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(downloadPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE).addComponent(installSettingsPanel));
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

		CommandExecutor executor = new CommandExecutor(configedMain, commands);
		executor.execute();
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
				.getCommand(Utils.getServerPathFromWebDAVPath(fileUploadCommand.getFullTargetPath()));
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
