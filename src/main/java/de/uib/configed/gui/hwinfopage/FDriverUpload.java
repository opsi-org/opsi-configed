package de.uib.configed.gui.hwinfopage;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.PersistenceController;
//import de.uib.utilities.*;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;

public class FDriverUpload extends SecondaryFrame {

	PanelDriverUpload panelDriverUpload;

	PersistenceController persist;
	ConfigedMain main;
	MainFrame mainframe;

	/*
	 * public FDriverUpload()
	 * {
	 * define();
	 * setGlobals(Globals.getMap());
	 * setTitle(Globals.APPNAME + " " +
	 * configed.getResourceValue("FDriverUpload.title"));
	 * 
	 * }
	 */

	public FDriverUpload(ConfigedMain main, PersistenceController persist, MainFrame mainframe) {
		super();

		this.main = main;
		this.mainframe = mainframe;
		this.persist = persist;

		define();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FDriverUpload.title"));

	}

	@Override
	public void start() {
		super.start();
	}

	protected void define() {
		panelDriverUpload = new PanelDriverUpload(main, persist, this);

		// main, persist, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelDriverUpload, 100,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		Containership cs_all = new Containership(getContentPane());
		cs_all.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backLightBlue }, JPanel.class);

		cs_all.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backgroundLightGrey },
				javax.swing.text.JTextComponent.class);

	}

	public void setUploadParameters(String byAuditPath) {
		panelDriverUpload.setByAuditPath(byAuditPath);

		logging.info(this, " setUploadParameters " + main.getSelectedClients()[0]);

		if (main.getSelectedClients() != null && main.getSelectedClients().length == 1)
			panelDriverUpload.setClientName(main.getSelectedClients()[0]);
		else
			panelDriverUpload.setClientName("");

		panelDriverUpload.setDepot(main.getConfigserver());
	}

}
