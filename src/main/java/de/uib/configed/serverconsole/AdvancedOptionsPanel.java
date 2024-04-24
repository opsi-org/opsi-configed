/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class AdvancedOptionsPanel extends JPanel {
	private JCheckBox jCheckBoxMD5Sum;
	private JCheckBox jCheckBoxZsync;
	private JCheckBox jCheckBoxSetRights;

	private boolean isAdvancedOpen = true;

	private boolean isGlobalReadOnly = PersistenceControllerFactory.getPersistenceController()
			.getUserRolesConfigDataService().isGlobalReadOnly();

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
		JLabel jLabelmd5sum = new JLabel(Configed.getResourceValue("AdvancedOptionsPanel.lbl_createMd5sum"));

		jCheckBoxMD5Sum = new JCheckBox();
		jCheckBoxMD5Sum.setSelected(true);
		jCheckBoxMD5Sum.setEnabled(!isGlobalReadOnly);
		JLabel jLabelzsync = new JLabel(Configed.getResourceValue("AdvancedOptionsPanel.lbl_createZsync"));

		jCheckBoxZsync = new JCheckBox();
		jCheckBoxZsync.setSelected(true);
		jCheckBoxZsync.setEnabled(!isGlobalReadOnly);

		JLabel jLabelSetRights = new JLabel(Configed.getResourceValue("AdvancedOptionsPanel.setRights"));

		jCheckBoxSetRights = new JCheckBox();
		jCheckBoxSetRights.setSelected(true);

		GroupLayout advancedOptionsPanelLayout = new GroupLayout(this);
		advancedOptionsPanelLayout.setAutoCreateGaps(true);
		advancedOptionsPanelLayout.setAutoCreateContainerGaps(true);
		setLayout(advancedOptionsPanelLayout);

		advancedOptionsPanelLayout.setHorizontalGroup(advancedOptionsPanelLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE)
				.addGroup(advancedOptionsPanelLayout.createParallelGroup()
						.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
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
				.addGap(Globals.GAP_SIZE)
				.addGroup(advancedOptionsPanelLayout.createParallelGroup()
						.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxZsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(advancedOptionsPanelLayout.createParallelGroup()
						.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxMD5Sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(advancedOptionsPanelLayout.createParallelGroup()
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));
	}

	public void showAdvancedSettings() {
		isAdvancedOpen = !isAdvancedOpen;
		setVisible(isAdvancedOpen);
	}
}
