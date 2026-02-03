/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import javax.swing.JPanel;

import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;

public abstract class AbstractSingleClientInfoPanel extends JPanel {
	protected KindOfExport kindOfExport;
	protected String exportFilename;
	protected boolean askForOverwrite;

	public void setWriteToFile(String path) {
		this.exportFilename = path;
	}

	public void setKindOfExport(KindOfExport kind) {
		this.kindOfExport = kind;
	}

	public void setAskForOverwrite(boolean b) {
		this.askForOverwrite = b;
	}

	public abstract SingleClientExporter getSingleClientExporter();

	protected abstract void updateContent(String client);
}
