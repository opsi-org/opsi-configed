/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;

public class SWMultiClientReportPanel extends BaseMultiClientReportPanel {
	private JCheckBox checkWithMsUpdates;
	private JCheckBox checkWithMsUpdates2;

	public SWMultiClientReportPanel() {
		super(Configed.getResourceValue("PanelSWMultiClientReport.title2"), SWcsvExporter.EXPORT_FILE_PREFIX,
				"swaudit_kind_of_export", "swaudit_export_dir", "swaudit_export_file_prefix",
				"swaudit_export_allow_overwrite");
	}

	public boolean wantsWithMsUpdates() {
		return checkWithMsUpdates.isSelected();
	}

	public boolean wantsWithMsUpdates2() {
		return checkWithMsUpdates2.isSelected();
	}

	@Override
	protected void addCustomOptions(JPanel panel) {
		JLabel labelWithMsUpdates = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates"));

		JLabel labelWithMsUpdates2 = new JLabel(Configed.getResourceValue("PanelSWMultiClientReport.withMsUpdates2"));

		checkWithMsUpdates = new JCheckBox();
		checkWithMsUpdates.addItemListener(e -> Configed.getSavedStates().setProperty("swaudit_export_ms_updates",
				Boolean.toString(checkWithMsUpdates.isSelected())));

		checkWithMsUpdates2 = new JCheckBox();
		checkWithMsUpdates2.addItemListener(e -> Configed.getSavedStates().setProperty("swaudit_export_ms_updates2",
				Boolean.toString(checkWithMsUpdates2.isSelected())));

		panel.add(checkWithMsUpdates, "split 2, gaptop " + Globals.MIN_GAP_SIZE + ", gapright " + Globals.GAP_SIZE);
		panel.add(labelWithMsUpdates, "wrap");

		panel.add(checkWithMsUpdates2, "split 2, gapright " + Globals.GAP_SIZE);
		panel.add(labelWithMsUpdates2, "wrap");
	}
}
