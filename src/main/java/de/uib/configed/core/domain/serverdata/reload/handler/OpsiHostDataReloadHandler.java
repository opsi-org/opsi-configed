/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class OpsiHostDataReloadHandler extends AbstractReloadHandler {
	public OpsiHostDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		// This must be cleared so that the clients for the depots are updated
		dataServices.cacheManager.clearCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS);

		// Both of these caches will be reloaded in the method 
		// retrieveFNode2TreeparentsPD. That's why it should not be parallelized.
		dataServices.cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		dataServices.hostInfoCollections.retrieveFNode2TreeparentsPD();
	}
}
