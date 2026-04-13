/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.infopage.AbstractMultiClientExporter;
import de.uib.configed.gui.type.SWAuditClientEntry;

public class SWExporter extends AbstractMultiClientExporter<PanelSWSingleClientInfo, SWMultiClientReportPanel> {
	public SWExporter(SWMultiClientReportPanel reportPanel, PanelSWSingleClientInfo panelInfo,
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

	@Override
	protected Set<String> getClients() {
		return PersistenceControllerFactory.getPersistenceController().getDataServices().software
				.getSoftwareAuditOnClients(configedMain.getSelectedClients()).keySet();
	}
}
