/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import de.uib.opsicommand.terminalcommand.TerminalCommandFactory;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class PMInstallPanel extends JPanel {
	private boolean isOpen;

	protected List<String> additionalDefaultPaths = new ArrayList<>();

	protected String workbench;

	public PMInstallPanel() {
		additionalDefaultPaths.add(TerminalCommandFactory.OPSI_PATH_VAR_REPOSITORY);

		workbench = PersistenceControllerFactory.getPersistenceController().getConfigDataService()
				.getConfigedWorkbenchDefaultValuePD();
		if (workbench.charAt(workbench.length() - 1) != '/') {
			workbench = workbench + "/";
		}
	}

	protected void isOpen(boolean isOpen) {
		this.isOpen = isOpen;
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
