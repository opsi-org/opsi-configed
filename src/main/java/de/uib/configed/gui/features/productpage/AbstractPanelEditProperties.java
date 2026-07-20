/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import javax.swing.JPanel;

import de.uib.configed.gui.share.datapanel.KeyValueTable;

public abstract class AbstractPanelEditProperties extends JPanel {
	protected KeyValueTable productPropertiesPanel;

	protected String productEdited;

	protected AbstractPanelEditProperties(KeyValueTable productPropertiesPanel) {
		super();
		this.productPropertiesPanel = productPropertiesPanel;
	}

	public abstract JPanel getTitlePanel();

	public abstract void setTitlePanelActivated(boolean actived);
}
