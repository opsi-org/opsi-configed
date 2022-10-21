/*
 * MultiTablePanel.java
 */

package de.uib.configed.gui.licences;

import de.uib.configed.*;
import de.uib.utilities.logging.*;

/**
 * Copyright (C) 2009 uib.de
 * @author roeder
 */
public class MultiTablePanel extends de.uib.utilities.swing.tabbedpane.TabClientAdapter
{
	protected ControlMultiTablePanel controller;

    public MultiTablePanel(ControlMultiTablePanel controller) {
		this.controller = controller;
    }
	
	public void reset()
	{
		super.reset();
		//logging.debug(this, "MultiTablePanel.reset() ");
		controller.refreshTables();
		controller.initializeVisualSettings();
	}
	
	public boolean mayLeave()
	{
		//logging.debug(this, "we want to leave " + this);
		
		if (de.uib.configed.Globals.isGlobalReadOnly())
			return true;
		
		boolean result = super.mayLeave();
		if (result)
			result = controller.mayLeave();
		
		return result;
	}

}
