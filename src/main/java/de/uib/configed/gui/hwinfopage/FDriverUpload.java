package de.uib.configed.gui.hwinfopage;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;

public class FDriverUpload extends SecondaryFrame {

	PanelDriverUpload panelDriverUpload;

	AbstractPersistenceController persist;
	ConfigedMain main;
	MainFrame mainframe;

	public FDriverUpload(ConfigedMain main, AbstractPersistenceController persist, MainFrame mainframe) {
		super();

		this.main = main;
		this.mainframe = mainframe;
		this.persist = persist;

		init();
		super.setGlobals(Globals.getMap());
		super.setTitle(Globals.APPNAME + " " + Configed.getResourceValue("FDriverUpload.title"));
	}

	private void init() {
		panelDriverUpload = new PanelDriverUpload(main, persist, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelDriverUpload, 100,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		if (!ConfigedMain.OPSI_4_3) {
			Containership containerShipAll = new Containership(getContentPane());
			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_7 }, JPanel.class);

			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_3 }, JTextComponent.class);
		}
	}

	public void setUploadParameters(String byAuditPath) {
		panelDriverUpload.setByAuditPath(byAuditPath);

		Logging.info(this, " setUploadParameters " + main.getSelectedClients()[0]);

		if (main.getSelectedClients() != null && main.getSelectedClients().length == 1) {
			panelDriverUpload.setClientName(main.getSelectedClients()[0]);
		} else {
			panelDriverUpload.setClientName("");
		}

		panelDriverUpload.setDepot(main.getConfigserver());
	}

}
