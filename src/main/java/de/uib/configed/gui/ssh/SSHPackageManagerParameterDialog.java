/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class SSHPackageManagerParameterDialog extends FGeneralDialog {
	protected JPanel buttonPanel = new JPanel();
	protected JLabel jLabelVerbosity = new JLabel();

	protected JButton jButtonExecute;

	protected ConfigedMain configedMain;

	public SSHPackageManagerParameterDialog(String title) {
		super(ConfigedMain.getMainFrame(), title);

		super.setTitle(title);
		super.setIconImage(Utils.getMainIcon());

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	protected void setComponentsEnabled(boolean value) {
		if (jButtonExecute != null) {
			jButtonExecute.setEnabled(value);
		}
	}

	protected void initLabels() {
		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller) {
		jButtonExecute = new JButton(Configed.getResourceValue("SSHConnection.buttonExec"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
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

		JButton jButtonReload = new JButton(Configed.getResourceValue("SSHConnection.buttonPackagesReload"));
		jButtonReload.setToolTipText(Configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonReload.addActionListener((ActionEvent actionEvent) -> {
				Logging.debug(this, "ActionEvent on btn_reload");
				configedMain.reload();
				consolidate();
			});
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));

		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonReload);
		buttonPanel.add(jButtonExecute);

		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());
	}

	protected void consolidate() {
		configedMain.reload();
	}

	private void cancel() {
		super.doAction1();
	}
}
