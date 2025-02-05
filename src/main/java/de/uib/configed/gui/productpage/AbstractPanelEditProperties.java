/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import javax.swing.JPanel;

import de.uib.utils.datapanel.DefaultEditMapPanel;

public abstract class AbstractPanelEditProperties extends JPanel {
	protected DefaultEditMapPanel productPropertiesPanel;

	protected String productEdited;

	protected AbstractPanelEditProperties(DefaultEditMapPanel productPropertiesPanel) {
		super();
		this.productPropertiesPanel = productPropertiesPanel;
	}

	public abstract JPanel getTitlePanel();

	public abstract void setTitlePanelActivated(boolean actived);
}
