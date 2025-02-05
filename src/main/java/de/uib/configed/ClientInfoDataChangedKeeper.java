/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Map;
import java.util.Map.Entry;

import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.DataChangedKeeper;
import de.uib.utils.logging.Logging;

public class ClientInfoDataChangedKeeper extends DataChangedKeeper {
	private Map<?, ?> source;

	private ConfigedMain configedMain;
	private HostInfo hostInfo;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientInfoDataChangedKeeper(ConfigedMain configedMain, HostInfo hostInfo) {
		this.configedMain = configedMain;
		this.hostInfo = hostInfo;
	}

	// we use this, but it would not override, therefore we perform a cast
	// it does not guarantee that the values of the map are maps!
	@Override
	public void dataHaveChanged(Object source1) {
		this.source = (Map<?, ?>) source1;

		Logging.debug(this, "dataHaveChanged source ", source);

		if (source == null) {
			Logging.info(this, "dataHaveChanged null");
		} else {
			for (Entry<?, ?> clientEntry : source.entrySet()) {
				Logging.debug(this, "dataHaveChanged for client ", clientEntry.getKey(), " with values",
						clientEntry.getValue());
			}
		}

		super.dataHaveChanged(source);

		Logging.debug(this, "dataHaveChanged dataChanged ", dataChanged);

		ChangedDataManager.setDataChanged(super.isDataChanged());

		// anyDataChanged in ConfigedMain
		Logging.info(this, "dataHaveChanged dataChanged ", dataChanged);
	}

	public void save() {
		Logging.info(this, "save , dataChanged ", dataChanged, " source ", source);
		if (this.dataChanged && source != null) {
			Logging.info(this, "save for clients ", configedMain.getSelectedClients().size());

			for (String client : configedMain.getSelectedClients()) {
				hostInfo.showAndSaveInternally(configedMain.getClientTablePanel(), client,
						(Map<?, ?>) source.get(client));
			}
			persistenceController.getHostDataService().updateHosts();

			source.clear();
			// we have to clear the map instead of nulling,
			// since otherwise changedClientInfo in MainFrame keep its value
			// such producing wrong values for other clients
		}

		this.dataChanged = false;
	}
}
