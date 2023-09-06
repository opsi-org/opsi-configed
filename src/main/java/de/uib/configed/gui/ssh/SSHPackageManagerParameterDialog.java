/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class SSHPackageManagerParameterDialog extends FGeneralDialog {

	protected int frameWidth = 900;
	protected int frameHeight = 600;

	protected JPanel buttonPanel = new JPanel();
	protected JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelFreeInput = new JLabel();

	private JButton jButtonHelp;
	protected JButton jButtonExecute;

	protected ConfigedMain configedMain;

	public SSHPackageManagerParameterDialog(String title) {
		super(null, title);

		super.setTitle(title);
		if (!Main.FONT) {
			super.setFont(Globals.DEFAULT_FONT);
		}
		super.setIconImage(Utils.getMainIcon());

		super.setSize(new Dimension(Globals.DIALOG_FRAME_DEFAULT_WIDTH, frameHeight));
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		if (!Main.THEMES) {
			super.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	protected void setComponentsEnabled(boolean value) {
		if (jButtonHelp != null) {
			jButtonHelp.setEnabled(value);
		}

		if (jButtonExecute != null) {
			jButtonExecute.setEnabled(value);
		}
	}

	protected void initLabels() {
		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jLabelFreeInput.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller) {

		jButtonHelp = new JButton("", Utils.createImageIcon("images/help-about.png", ""));
		jButtonHelp.setText(Configed.getResourceValue("SSHConnection.buttonHelp"));

		jButtonHelp.addActionListener(actionEvent -> doActionHelp(caller));

		jButtonExecute = new JButton();
		jButtonExecute.setText(Configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Utils.createImageIcon("images/execute16_blue.png", ""));
		if (!PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly()) {
			jButtonExecute.addActionListener((ActionEvent actionEvent) -> {
				if (caller instanceof SSHPackageManagerUninstallParameterDialog) {
					((SSHPackageManagerUninstallParameterDialog) caller).doAction3();
				} else if (caller instanceof SSHPackageManagerInstallParameterDialog) {
					((SSHPackageManagerInstallParameterDialog) caller).doAction3();
				} else {
					Logging.warning(this, "caller has unexpected class " + caller.getClass());
				}
			});
		}

		JButton jButtonReload = new JButton();
		jButtonReload.setText(Configed.getResourceValue("SSHConnection.buttonPackagesReload"));
		jButtonReload.setIcon(Utils.createImageIcon("images/reloadcomplete16.png", ""));
		jButtonReload.setToolTipText(Configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));

		if (!PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly()) {
			jButtonReload.addActionListener((ActionEvent actionEvent) -> {
				Logging.debug(this, "ActionEvent on btn_reload");
				configedMain.reload();
				consolidate();
			});
		}

		JButton jButtonClose = new JButton();
		jButtonClose.setText(Configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Utils.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonReload);
		buttonPanel.add(jButtonExecute);

		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly());
	}

	protected void consolidate() {
		configedMain.reload();
	}

	private void doActionHelp(final SSHPackageManagerParameterDialog caller) {
		SSHConnectionExecDialog dia = null;
		if (caller instanceof SSHPackageManagerUninstallParameterDialog) {
			dia = new CommandOpsiPackageManagerUninstall().startHelpDialog();
		} else if (caller instanceof SSHPackageManagerInstallParameterDialog) {
			dia = new CommandOpsiPackageManagerInstall().startHelpDialog();
		} else {
			Logging.warning(this, "caller has unexpected class " + caller.getClass());
		}

		if (dia != null) {
			dia.setVisible(true);
		}
	}

	private void cancel() {
		super.doAction1();
	}
}
