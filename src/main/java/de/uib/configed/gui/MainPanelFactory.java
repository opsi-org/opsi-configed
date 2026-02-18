/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

public class MainPanelFactory {

	private TopToolBarManager topToolBarManager;

	public MainPanelFactory(ConfigedMain configedMain) {
		topToolBarManager = new TopToolBarManager(configedMain);
	}
}