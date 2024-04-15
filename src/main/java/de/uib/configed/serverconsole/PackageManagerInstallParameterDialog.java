/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandFileUpload;
import de.uib.configed.serverconsole.command.SingleCommandOpsiPackageManagerInstall;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class PackageManagerInstallParameterDialog extends PackageManagerParameterDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel radioPanel = new JPanel();
	private PMInstallLocalPanel installLocalPanel;
	private PMInstallServerPanel installServerPanel;
	private PMInstallCurlPanel installCurlPanel;
	private PMInstallSettingsPanel installSettingsPanel;
	private JLabel jLabelInstall = new JLabel();

	private JRadioButton jRadioButtonLocal;
	private JRadioButton jRadioButtonServer;
	private JRadioButton jRadioButtonCurl;

	public PackageManagerInstallParameterDialog(ConfigedMain configedMain) {
		super(Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.title"));

		this.configedMain = configedMain;

		initInstances();
		init();
		initLayout();

		super.setSize(900, 600);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setVisible(true);
	}

	private void initInstances() {
		super.initButtons(this);
		ButtonGroup group = new ButtonGroup();

		jRadioButtonLocal = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromLocal"));
		jRadioButtonServer = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromServer"));
		jRadioButtonCurl = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetFrom"));
		group.add(jRadioButtonLocal);
		group.add(jRadioButtonServer);
		group.add(jRadioButtonCurl);

		installLocalPanel = new PMInstallLocalPanel();
		installServerPanel = new PMInstallServerPanel();
		installCurlPanel = new PMInstallCurlPanel();
		installSettingsPanel = new PMInstallSettingsPanel(this);

		jRadioButtonLocal.setSelected(true);

		installLocalPanel.isOpen(false);
		installLocalPanel.open();

		installServerPanel.isOpen(true);
		installServerPanel.close();

		installCurlPanel.isOpen(true);
		installCurlPanel.close();

		jRadioButtonLocal.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.open();
			installServerPanel.close();
			installCurlPanel.close();
		});
		jRadioButtonServer.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.open();
			installCurlPanel.close();
		});
		jRadioButtonCurl.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.close();
			installCurlPanel.open();
		});
	}

	private void initLayout() {
		GroupLayout radioPanelLayout = new GroupLayout(radioPanel);
		radioPanel.setLayout(radioPanelLayout);

		radioPanelLayout.setHorizontalGroup(radioPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(jRadioButtonServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(jRadioButtonCurl, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installCurlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(2 * Globals.GAP_SIZE));

		radioPanelLayout.setVerticalGroup(radioPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonServer, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonCurl, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installCurlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup()

				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
						.addComponent(jLabelInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(radioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainPanelLayout
						.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(installSettingsPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)));

		mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addComponent(jLabelInstall).addGap(Globals.GAP_SIZE).addComponent(radioPanel).addGap(Globals.GAP_SIZE)
				.addComponent(installSettingsPanel).addGap(2 * Globals.GAP_SIZE));
	}

	private void init() {
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		radioPanel.setBorder(BorderFactory.createTitledBorder(""));
		jLabelInstall.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelInstall"));
	}

	@Override
	public void doAction3() {
		Logging.info(this, " doAction3 install ");
		MultiCommandTemplate commands = new MultiCommandTemplate();
		commands.setMainName("PackageInstallation");
		SingleCommandOpsiPackageManagerInstall pmInstallCom;

		if (jRadioButtonLocal.isSelected()) {
			pmInstallCom = handleLocalFileUpload(commands);
		} else if (jRadioButtonServer.isSelected()) {
			pmInstallCom = handleServerFileUpload(commands);
		} else {
			pmInstallCom = handleCurlFileUpload(commands);
		}

		if (pmInstallCom == null) {
			return;
		}

		installSettingsPanel.updateCommand(pmInstallCom);

		CommandExecutor executor = new CommandExecutor(configedMain);
		executor.executeMultiCommand(commands);
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
		Logging.info(this, "c " + pmInstallCom);
		if (pmInstallCom != null) {
			commands.addCommand(pmInstallCom);
		} else {
			Logging.warning(this, "ERROR 3 command = null");
		}
		return pmInstallCom;
	}
}
