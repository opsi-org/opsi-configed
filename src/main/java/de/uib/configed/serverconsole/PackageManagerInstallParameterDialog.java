/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.itextpdf.text.Font;

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
	private JPanel radioPanel = new JPanel();
	private PMInstallLocalPanel installLocalPanel;
	private PMInstallServerPanel installServerPanel;
	private PMInstallCurlPanel installCurlPanel;
	private PMInstallSettingsPanel installSettingsPanel;
	private JLabel jLabelInstall = new JLabel();

	private JRadioButton jRadioButtonLocal;
	private JRadioButton jRadioButtonServer;
	private JRadioButton jRadioButtonCurl;
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

		dialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("buttonExecute"))) {
			execute();
		}
	}

	private void initInstances() {
		ButtonGroup group = new ButtonGroup();

		jRadioButtonLocal = new JRadioButton(
				Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromLocal"));
		jRadioButtonServer = new JRadioButton(
				Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelFromServer"));
		jRadioButtonCurl = new JRadioButton(Configed
				.getResourceValue("PackageManagerInstallParameterDialog.opsipackagemanager_install.jLabelCurlFrom"));
		group.add(jRadioButtonLocal);
		group.add(jRadioButtonServer);
		group.add(jRadioButtonCurl);

		installLocalPanel = new PMInstallLocalPanel();
		installServerPanel = new PMInstallServerPanel(fromMakeProductfile);
		installCurlPanel = new PMInstallCurlPanel();
		installSettingsPanel = new PMInstallSettingsPanel(dialog);

		if (fromMakeProductfile != null && !fromMakeProductfile.isEmpty()) {
			jRadioButtonServer.setSelected(true);

			installLocalPanel.isOpen(true);
			installLocalPanel.close();

			installServerPanel.isOpen(false);
			installServerPanel.open();

			installServerPanel.setPackagePath(fromMakeProductfile);
		} else {
			jRadioButtonLocal.setSelected(true);

			installLocalPanel.isOpen(false);
			installLocalPanel.open();

			installServerPanel.isOpen(true);
			installServerPanel.close();
		}

		installCurlPanel.isOpen(true);
		installCurlPanel.close();

		jRadioButtonLocal.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.open();
			installServerPanel.close();
			installCurlPanel.close();
			dialog.pack();
		});
		jRadioButtonServer.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.open();
			installCurlPanel.close();
			dialog.pack();
		});
		jRadioButtonCurl.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.close();
			installCurlPanel.open();
			dialog.pack();
		});
	}

	private void initLayout() {
		radioPanel.setBorder(BorderFactory.createTitledBorder(""));
		jLabelInstall.setText(Configed.getResourceValue("PackageManagerInstallParameterDialog.jLabelInstall"));
		jLabelInstall.setFont(jLabelInstall.getFont().deriveFont(Font.BOLD));

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
				.addGap(Globals.GAP_SIZE));

		radioPanelLayout.setVerticalGroup(radioPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonCurl, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installCurlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup()
				.addComponent(jLabelInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(radioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(installSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createSequentialGroup().addComponent(jLabelInstall).addGap(Globals.GAP_SIZE)
						.addComponent(radioPanel).addGap(Globals.GAP_SIZE).addComponent(installSettingsPanel));
	}

	private void execute() {
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
