/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.infopage;

import javax.swing.JPanel;

import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
import de.uib.configed.gui.share.table.AbstractExportTable.OverwriteDecision;

public abstract class AbstractSingleClientInfoPanel extends JPanel {
	protected KindOfExport kindOfExport = KindOfExport.CSV;
	protected String exportFilename;
	protected boolean askForOverwrite;
	protected OverwriteDecision decision;

	public void setWriteToFile(String path) {
		this.exportFilename = path;
	}

	public void setKindOfExport(KindOfExport kind) {
		this.kindOfExport = kind;
	}

	public void setAskForOverwrite(boolean b) {
		this.askForOverwrite = b;
	}

	public void setOverwriteDecision(OverwriteDecision decision) {
		this.decision = decision;
	}

	public abstract SingleClientExporter getSingleClientExporter();

	protected abstract void updateContent(String client);
}
