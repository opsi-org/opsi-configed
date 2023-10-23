/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class SSHPMInstallPanel extends JPanel {
	protected boolean isOpen;

	protected List<String> additionalDefaultPaths = new ArrayList<>();

	protected String workbench;

	public SSHPMInstallPanel() {

		additionalDefaultPaths.add(SSHCommandFactory.OPSI_PATH_VAR_REPOSITORY);

		workbench = PersistenceControllerFactory.getPersistenceController().getConfigDataService()
				.getConfigedWorkbenchDefaultValuePD();
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
