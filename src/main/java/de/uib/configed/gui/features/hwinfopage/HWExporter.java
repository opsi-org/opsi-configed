/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.infopage.AbstractMultiClientExporter;

public class HWExporter extends AbstractMultiClientExporter<PanelHWSingleClientInfo, BaseMultiClientReportPanel> {
	public HWExporter(BaseMultiClientReportPanel reportPanel, PanelHWSingleClientInfo panelInfo,
			ConfigedMain configedMain) {
		super(panelInfo, reportPanel, configedMain);
	}

	@Override
	protected String getExportPrefixKey() {
		return "hwaudit_export_file_prefix";
	}

	@Override
	protected void applyExtraSettings() {
		// no extra HW-specific settings
	}

	@Override
	protected void updatePanelForClient(String client) {
		panelInfo.updateContent(client);
	}

	@Override
	protected String getScanDateForClient(String client) {
		Map<String, List<Map<String, Object>>> hardwareInfo = PersistenceControllerFactory.getPersistenceController()
				.getDataServices().hardware.getHardwareInfo(client);

		List<Map<String, Object>> scanProperty = hardwareInfo.get(PanelHWSingleClientInfo.SCANPROPERTYNAME);
		return scanProperty != null ? scanProperty.get(0).get(PanelHWSingleClientInfo.SCANTIME).toString() : null;
	}

	@Override
	protected Set<String> getClients() {
		return PersistenceControllerFactory.getPersistenceController().getDataServices().hardware
				.getHardwareAuditOnClients(configedMain.getSelectedClients()).keySet();
	}
}
