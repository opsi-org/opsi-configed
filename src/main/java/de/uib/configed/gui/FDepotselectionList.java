/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utils.logging.Logging;

public class FDepotselectionList extends FGeneralDialog {
	private DepotsList depotsList;
	private Window masterWindow;

	public FDepotselectionList(JDialog masterWindow, ConfigedMain configedMain) {
		super(masterWindow, Configed.getResourceValue("FDepotselectionList.title"),
				new String[] { Configed.getResourceValue("buttonCancel"),
						Configed.getResourceValue("FDepotselectionList.buttontake") },
				500, 300);
		depotsList = new DepotsList(configedMain);
		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollpane.getViewport().add(depotsList);
		this.masterWindow = masterWindow;
		masterWindow.setVisible(true);
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return false;
	}

	public void setListData(List<String> v) {
		depotsList.setListData(v);
	}

	public List<String> getSelectedDepots() {
		return depotsList.getSelectedValuesList();
	}

	@Override
	public void doAction1() {
		Logging.debug(this, "doAction1");
		result = 1;
		masterWindow.setVisible(true);
		leave();
	}

	@Override
	public void doAction2() {
		Logging.debug(this, "doAction2");
		result = 2;
		masterWindow.setVisible(true);
		leave();
	}

	@Override
	public void leave() {
		setVisible(false);
		// we dont dispose the window, dispose it in the enclosing class
	}

	public void exit() {
		super.leave();
	}
}
