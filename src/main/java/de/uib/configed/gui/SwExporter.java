/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.hwinfopage.AbstractMultiClientExporter;
import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo;
import de.uib.configed.gui.features.swinfopage.SWMultiClientReportPanel;
import de.uib.configed.gui.type.SWAuditClientEntry;

public class SwExporter extends AbstractMultiClientExporter<PanelSWSingleClientInfo, SWMultiClientReportPanel> {
	public SwExporter(SWMultiClientReportPanel reportPanel, PanelSWSingleClientInfo panelInfo,
			ConfigedMain configedMain) {
		super(panelInfo, reportPanel, configedMain);
	}

	@Override
	protected String getExportPrefixKey() {
		return "swaudit_export_file_prefix";
	}

	@Override
	protected void applyExtraSettings() {
		panelInfo.setWithMsUpdates(reportPanel.wantsWithMsUpdates());
		panelInfo.setWithMsUpdates2(reportPanel.wantsWithMsUpdates2());
	}

	@Override
	protected void updatePanelForClient(String client) {
		panelInfo.setHost(client);
		panelInfo.updateModel();
	}

	@Override
	protected String getScanDateForClient(String client) {
		Map<String, List<SWAuditClientEntry>> swAuditClientEntries = PersistenceControllerFactory
				.getPersistenceController().getDataServices().software
						.getSoftwareAuditOnClients(Collections.singletonList(client));
		return PersistenceControllerFactory.getPersistenceController().getDataServices().software
				.getLastSoftwareAuditModification(swAuditClientEntries, client);
	}
}
