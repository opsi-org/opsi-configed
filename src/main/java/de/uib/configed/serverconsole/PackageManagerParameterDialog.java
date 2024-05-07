/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class PackageManagerParameterDialog extends FGeneralDialog {
	protected JPanel buttonPanel = new JPanel();

	protected JButton jButtonExecute;

	protected ConfigedMain configedMain;

	public PackageManagerParameterDialog(String title) {
		super(ConfigedMain.getMainFrame(), title);

		super.setTitle(title);
		super.setIconImage(Utils.getMainIcon());

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	protected void initButtons(final PackageManagerParameterDialog caller) {
		jButtonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		jButtonExecute.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonExecute.addActionListener((ActionEvent actionEvent) -> {
				if (caller instanceof PackageManagerUninstallParameterDialog) {
					((PackageManagerUninstallParameterDialog) caller).doAction3();
				} else if (caller instanceof PackageManagerInstallParameterDialog) {
					((PackageManagerInstallParameterDialog) caller).doAction3();
				} else {
					Logging.warning(this, "caller has unexpected class " + caller.getClass());
				}
			});
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));

		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonExecute);
	}

	protected void reload() {
		configedMain.reload();
	}

	private void cancel() {
		super.doAction1();
	}
}
