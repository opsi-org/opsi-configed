/*
 * MultiTablePanel.java
 */

package de.uib.configed.gui.licences;

import de.uib.configed.ControlMultiTablePanel;
import de.uib.configed.Globals;

/**
 * Copyright (C) 2009 uib.de
 * 
 * @author roeder
 */
public class MultiTablePanel extends de.uib.utilities.swing.tabbedpane.TabClientAdapter {
	protected ControlMultiTablePanel controller;

	public MultiTablePanel(ControlMultiTablePanel controller) {
		this.controller = controller;
	}

	@Override
	public void reset() {
		super.reset();

		controller.refreshTables();
		controller.initializeVisualSettings();
	}

	@Override
	public boolean mayLeave() {

		if (Globals.isGlobalReadOnly())
			return true;

		boolean result = super.mayLeave();
		if (result)
			result = controller.mayLeave();

		return result;
	}

}
