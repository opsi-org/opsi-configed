/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import de.uib.configed.serverconsole.command.CommandFactory;

public class PMInstallPanel extends JPanel {
	private boolean isOpen;

	protected List<String> additionalDefaultPaths = new ArrayList<>();
	protected String workbench;

	public PMInstallPanel() {
		additionalDefaultPaths.add(CommandFactory.WEBDAV_OPSI_PATH_VAR_REPOSITORY);
		workbench = CommandFactory.WEBDAV_OPSI_PATH_VAR_WORKBENCH;
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
