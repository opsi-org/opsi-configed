/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.gui.type.Object2GroupEntry;

public class HostDataReloadHandler extends AbstractReloadHandler {
	public HostDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		// Delete these client specific data
		cacheManager.clearCachedData(CacheIdentifier.SESSION_INFO);

		// This must be cleared so that the clients for the depots are updated
		cacheManager.clearCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS);

		ParallelTaskExecutor executor = new ParallelTaskExecutor();

		// Both of these caches will be reloaded in the method 
		// retrieveFNode2TreeparentsPD. That's why it should not be parallelized.
		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		executor.runInParallel(dataServices.hostInfoCollections::retrieveFNode2TreeparentsPD);

		cacheManager.clearCachedData(CacheIdentifier.HOST_CONFIGS);
		executor.runInParallel(dataServices.config::retrieveHostConfigsPD);

		cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS);
		executor.runInParallel(dataServices.group::retrieveHostGroupsPD);

		cacheManager.clearCachedData(CacheIdentifier.FHOST_TO_GROUPS);
		executor.runInParallel(dataServices.group::retrieveFObject2GroupsPD);

		cacheManager.clearCachedData(CacheIdentifier.FHOST_GROUP_TO_MEMBERS);
		executor.runInParallel(() -> dataServices.group.retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP,
				"clientId", CacheIdentifier.FHOST_GROUP_TO_MEMBERS));

		executor.waitForCompletion();
	}
}
