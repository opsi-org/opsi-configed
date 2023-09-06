/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;

public class SSHPMInstallPanel extends JPanel {
	protected boolean isOpen;

	protected List<String> additionalDefaultPaths = new ArrayList<>();

	protected String workbench;

	public SSHPMInstallPanel() {
		if (!Main.THEMES) {
			super.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		additionalDefaultPaths.add(SSHCommandFactory.OPSI_PATH_VAR_REPOSITORY);

		workbench = OpsiServiceNOMPersistenceController.getConfigedWorkbenchDefaultValue();
		if (workbench.charAt(workbench.length() - 1) != '/') {
			workbench = workbench + "/";
		}
	}

	public void open() {
		if (!isOpen) {
			this.setSize(this.getWidth(), this.getHeight() + this.getHeight());
			isOpen = true;
			this.setVisible(isOpen);
		}
	}

	public void close() {
		if (isOpen) {
			this.setSize(this.getWidth(), 0);
			isOpen = false;
			this.setVisible(isOpen);
		}
	}
}
