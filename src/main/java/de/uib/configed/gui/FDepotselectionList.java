package de.uib.configed.gui;

import java.awt.Window;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.ListSelectionModel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

public class FDepotselectionList extends FGeneralDialog {
	private DepotsList depotsList;

	protected Window masterWindow;

	public FDepotselectionList(JDialog masterWindow) {
		super(

				masterWindow, configed.getResourceValue("FDepotselectionList.title"),
				new String[] { configed.getResourceValue("FDepotselectionList.buttontake"),
						configed.getResourceValue("FDepotselectionList.buttonclose") },
				500, 300);
		depotsList = new DepotsList(PersistenceControllerFactory.getPersistenceController());
		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		init();
		this.masterWindow = masterWindow;
		masterWindow.setVisible(true);
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return false;
	}

	private void init() {
		jButton1.setIcon(Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""));
		jButton2.setIcon(Globals.createImageIcon("images/cancel16.png", ""));
		scrollpane.getViewport().add(depotsList);
	}

	public void setListData(Vector<? extends String> v) {
		depotsList.setListData(v);
	}

	public Vector<? extends String> getListData() {
		logging.info(this, "getListData() : " + depotsList.getListData());
		return depotsList.getListData();
	}

	public List<String> getSelectedDepots() {
		return depotsList.getSelectedValuesList();
	}

	public int[] getSelectedIndices() {
		return depotsList.getSelectedIndices();
	}

	@Override
	public void doAction1() {
		logging.debug(this, "doAction1");
		result = 1;
		masterWindow.setVisible(true);
		leave();
	}

	@Override
	public void doAction2() {
		logging.debug(this, "doAction2");
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
