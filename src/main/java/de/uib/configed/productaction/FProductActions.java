/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productaction;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class FProductActions extends SecondaryFrame {
	private ConfigedMain configedMain;

	public FProductActions(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		define();

		super.setIconImage(Utils.getMainIcon());
		super.setTitle(Configed.getResourceValue("FProductAction.title"));
	}

	private void define() {
		PanelInstallOpsiPackage panelInstallOpsiPackage = new PanelInstallOpsiPackage(configedMain, this);

		JPanel imageActionPanel = new JPanel();

		PanelCompleteWinProducts panelCompleteWinProducts = new PanelCompleteWinProducts(configedMain, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panelInstallOpsiPackage, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(panelCompleteWinProducts, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(panelInstallOpsiPackage, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(panelCompleteWinProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}
}
