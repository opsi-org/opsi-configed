/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JTabbedPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;

public class ServerConfiguration extends JTabbedPane {
	private PanelHostConfig panelHostConfig;

	public ServerConfiguration(ConfigedMain configedMain) {

		panelHostConfig = new PanelHostConfig(configedMain);
		super.addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);
	}
}
