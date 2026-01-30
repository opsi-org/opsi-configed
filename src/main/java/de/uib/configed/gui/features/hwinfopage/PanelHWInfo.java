/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.CardLayout;

import javax.swing.JPanel;

import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class PanelHWInfo extends AbstractConfigurationTab {
	private static final String SINGLE_CLIENT_HW_INFO = "SINGLE_CLIENT_HW_INFO";
	private static final String MULTI_CLIENT_HW_REPORT = "MULTI_CLIENT_HW_REPORT";

	private CardLayout cardLayout;
	private JPanel contentPanel;

	private PanelHWSingleClientInfo panelHWSingleClientInfo;

	private ConfigedMain configedMain;

	public PanelHWInfo(ConfigedMain configedMain) {
		super(true, true);
		this.configedMain = configedMain;

		panelHWSingleClientInfo = new PanelHWSingleClientInfo(configedMain, true);
		PanelHWMultiClientReport panelHWMultiClientReport = new PanelHWMultiClientReport();
		HwExporter swExporter = new HwExporter(panelHWMultiClientReport, panelHWSingleClientInfo, configedMain);
		panelHWMultiClientReport.setActionListenerForStart(swExporter);

		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		contentPanel.add(panelHWSingleClientInfo, SINGLE_CLIENT_HW_INFO);
		contentPanel.add(panelHWMultiClientReport, MULTI_CLIENT_HW_REPORT);

		super.setComponent(contentPanel);
	}

	@Override
	protected void updateContent() {
		int selectedClientsSize = configedMain.getSelectedClients().size();
		if (selectedClientsSize == 0) {
			// Do nothing, this should not happen, because it will be shown only if clients are selected
			Logging.warning(this, "updateContent: no clients selected, this should not happen");
		} else if (selectedClientsSize == 1) {
			panelHWSingleClientInfo.updateContent(configedMain.getSelectedClients().getFirst());
			cardLayout.show(contentPanel, SINGLE_CLIENT_HW_INFO);
		} else {
			cardLayout.show(contentPanel, MULTI_CLIENT_HW_REPORT);
		}
	}
}
