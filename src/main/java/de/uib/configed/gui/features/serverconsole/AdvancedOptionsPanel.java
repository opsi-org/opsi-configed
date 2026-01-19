/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import net.miginfocom.swing.MigLayout;

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

		setLayout(new MigLayout("insets " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		add(jCheckBoxZsync, "gapbottom " + Globals.GAP_SIZE);
		add(jCheckBoxMD5Sum, "gapbottom " + Globals.GAP_SIZE);
		add(jCheckBoxSetRights, "gapbottom " + Globals.GAP_SIZE);
	}
}
