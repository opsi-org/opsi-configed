/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.infopage;

import java.awt.CardLayout;

import javax.swing.JPanel;

import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;
import de.uib.configed.share.logging.Logging;
import lombok.Getter;

public class GenericAuditPanelInfo<S extends AbstractSingleClientInfoPanel, M extends BaseMultiClientReportPanel>
		extends AbstractConfigurationTab {
	protected static final String SINGLE_CLIENT = "SINGLE_CLIENT";
	protected static final String MULTI_CLIENT = "MULTI_CLIENT";

	protected final ConfigedMain configedMain;

	@Getter
	protected final S singleClientPanel;

	protected final M multiClientPanel;

	protected final CardLayout cardLayout = new CardLayout();
	protected final JPanel contentPanel = new JPanel(cardLayout);

	@SuppressWarnings("java:S1699")
	public GenericAuditPanelInfo(ConfigedMain configedMain, S singleClientPanel, M multiClientPanel,
			AbstractMultiClientExporter<S, M> exporter) {
		super(true, true);
		this.configedMain = configedMain;
		this.singleClientPanel = singleClientPanel;
		this.multiClientPanel = multiClientPanel;

		multiClientPanel.setActionListenerForStart(exporter);

		contentPanel.add(singleClientPanel, SINGLE_CLIENT);
		contentPanel.add(multiClientPanel, MULTI_CLIENT);

		setComponent(contentPanel);
	}

	@Override
	protected final void updateContent() {
		int count = configedMain.getSelectedClients().size();

		if (count == 0) {
			Logging.warning(this, "updateContent: no clients selected, this should not happen");
			return;
		}

		if (count == 1) {
			singleClientPanel.updateContent(configedMain.getSelectedClients().getFirst());
			cardLayout.show(contentPanel, SINGLE_CLIENT);
		} else {
			cardLayout.show(contentPanel, MULTI_CLIENT);
		}
	}
}
