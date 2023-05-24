
package de.uib.configed.productaction;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;

public class FProductActions extends SecondaryFrame {

	private OpsiserviceNOMPersistenceController persist;
	private ConfigedMain main;

	public FProductActions(ConfigedMain main, OpsiserviceNOMPersistenceController persist) {
		super();

		this.main = main;
		this.persist = persist;

		define();

		super.setGlobals(Globals.getMap());
		super.setTitle(Globals.APPNAME + " " + Configed.getResourceValue("FProductAction.title"));
	}

	private void define() {
		PanelInstallOpsiPackage panelInstallOpsiPackage = new PanelInstallOpsiPackage(main, persist, this);

		JPanel imageActionPanel = new JPanel();

		PanelCompleteWinProducts panelCompleteWinProducts = new PanelCompleteWinProducts(main, persist, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panelInstallOpsiPackage, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(panelCompleteWinProducts, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(panelInstallOpsiPackage, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(panelCompleteWinProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		if (!Main.THEMES) {
			Containership containerShipAll = new Containership(getContentPane());
			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_7 }, JPanel.class);

			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_3 }, JTextComponent.class);
		}
	}
}
