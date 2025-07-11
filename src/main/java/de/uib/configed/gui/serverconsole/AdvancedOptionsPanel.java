/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.serverconsole;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;

public class AdvancedOptionsPanel extends JPanel {
	private JCheckBox jCheckBoxMD5Sum;
	private JCheckBox jCheckBoxZsync;
	private JCheckBox jCheckBoxSetRights;

	public AdvancedOptionsPanel() {
		initGUI();
	}

	public boolean useZsync() {
		return jCheckBoxZsync.isSelected();
	}

	public boolean useMD5Sum() {
		return jCheckBoxMD5Sum.isSelected();
	}

	public boolean setRights() {
		return jCheckBoxSetRights.isSelected();
	}

	private void initGUI() {
		setBorder(BorderFactory.createTitledBorder(""));

		jCheckBoxMD5Sum = new JCheckBox(Configed.getResourceValue("AdvancedOptionsPanel.lbl_createMd5sum"), true);

		jCheckBoxZsync = new JCheckBox(Configed.getResourceValue("AdvancedOptionsPanel.lbl_createZsync"), true);

		jCheckBoxSetRights = new JCheckBox(Configed.getResourceValue("AdvancedOptionsPanel.setRights"), true);

		GroupLayout advancedOptionsPanelLayout = new GroupLayout(this);
		advancedOptionsPanelLayout.setAutoCreateGaps(true);
		advancedOptionsPanelLayout.setAutoCreateContainerGaps(true);
		setLayout(advancedOptionsPanelLayout);

		advancedOptionsPanelLayout.setHorizontalGroup(advancedOptionsPanelLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE)
				.addGroup(advancedOptionsPanelLayout.createParallelGroup()
						.addComponent(jCheckBoxZsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxMD5Sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));
		advancedOptionsPanelLayout.setVerticalGroup(advancedOptionsPanelLayout.createSequentialGroup()
				.addComponent(jCheckBoxZsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jCheckBoxMD5Sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}
}
