/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

import java.awt.CardLayout;

import javax.swing.JPanel;

import de.uib.configed.gui.AbstractClientConfigurationTab;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.SwExporter;
import de.uib.configed.share.logging.Logging;

public class PanelSWInfo extends AbstractClientConfigurationTab {
	private static final String SINGLE_CLIENT_SW_INFO = "SINGLE_CLIENT_SW_INFO";
	private static final String MULTI_CLIENT_SW_REPORT = "MULTI_CLIENT_SW_REPORT";

	private CardLayout cardLayout;
	private JPanel contentPanel;

	private PanelSWSingleClientInfo panelSWSingleClientInfo;

	private ConfigedMain configedMain;

	public PanelSWInfo(ConfigedMain configedMain) {
		super(true);
		this.configedMain = configedMain;

		panelSWSingleClientInfo = new PanelSWSingleClientInfo(configedMain, true);
		PanelSWMultiClientReport panelSWMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(panelSWMultiClientReport, panelSWSingleClientInfo, configedMain);
		panelSWMultiClientReport.setActionListenerForStart(swExporter);

		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		contentPanel.add(panelSWSingleClientInfo, SINGLE_CLIENT_SW_INFO);
		contentPanel.add(panelSWMultiClientReport, MULTI_CLIENT_SW_REPORT);

		super.setComponent(contentPanel);
	}

	@Override
	protected void updateContent() {
		int selectedClientsSize = configedMain.getSelectedClients().size();
		if (selectedClientsSize == 0) {
			// Do nothing, this should not happen, because it will be shown only if clients are selected
			Logging.warning(this, "updateContent: no clients selected, this should not happen");
		} else if (selectedClientsSize == 1) {
			panelSWSingleClientInfo.updateContent(configedMain.getSelectedClients().getFirst());
			cardLayout.show(contentPanel, SINGLE_CLIENT_SW_INFO);
		} else {
			cardLayout.show(contentPanel, MULTI_CLIENT_SW_REPORT);
		}
	}
}
