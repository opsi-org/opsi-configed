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
		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		hostInfoCollections.retrieveOpsiHostsPD();

		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		hostInfoCollections.retrieveFNode2TreeparentsPD();
	}
}
