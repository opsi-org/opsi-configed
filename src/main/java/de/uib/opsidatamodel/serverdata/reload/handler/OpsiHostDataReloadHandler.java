/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;

public class OpsiHostDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private HostInfoCollections hostInfoCollections;

	public OpsiHostDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	@Override
	public void handle(String event) {
		// Both of these caches will be reloaded in the method 
		// retrieveFNode2TreeparentsPD. That's why it should not be parallelized.
		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		hostInfoCollections.retrieveFNode2TreeparentsPD();
	}
}
