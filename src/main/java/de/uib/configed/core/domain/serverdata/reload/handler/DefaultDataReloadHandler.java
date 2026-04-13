/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.gui.type.Object2GroupEntry;

/**
 * Implementation of {@link AbstractReloadHandler} which is responsible for
 * reloading one entry in the internal cache.
 * <p>
 * This {@link AbstractReloadHandler} implementation is triggered by
 * {@link CacheIdentifier}. Not all {@link CacheIdentifier} have to be handled
 * in this {@link AbstractReloadHandler}, only those that are required.
 */
public class DefaultDataReloadHandler extends AbstractReloadHandler {
	private Map<String, Consumer<Void>> eventHandlers;

	public DefaultDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.eventHandlers = new HashMap<>();
		registerHandlers();
	}

	private void registerHandlers() {
		eventHandlers.put(CacheIdentifier.LICENSE_USAGE.toString(), (Void v) -> {
			dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
			dataServices.license.retrieveLicenseUsagesPD();
		});
		eventHandlers.put(CacheIdentifier.FHOST_GROUP_TO_MEMBERS.toString(), (Void v) -> {
			dataServices.cacheManager.clearCachedData(CacheIdentifier.FHOST_GROUP_TO_MEMBERS);
			dataServices.group.retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId",
					CacheIdentifier.FHOST_GROUP_TO_MEMBERS);
		});
		eventHandlers.put(CacheIdentifier.HOST_GROUPS.toString(), (Void v) -> {
			dataServices.cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS);
			dataServices.group.retrieveHostGroupsPD();
		});
		eventHandlers.put(CacheIdentifier.HOST_CONFIGS.toString(), (Void v) -> {
			dataServices.cacheManager.clearCachedData(CacheIdentifier.HOST_CONFIGS);
			dataServices.config.retrieveHostConfigsPD();
		});
		eventHandlers.put(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString(),
				(Void v) -> dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTY_STATES));
		eventHandlers.put(CacheIdentifier.ALL_DATA.toString(), (Void v) -> dataServices.cacheManager.clearForReload());
		eventHandlers.put(CacheIdentifier.LICENSES.toString(), (Void v) -> {
			dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSES);
			dataServices.license.retrieveLicensesPD();
		});
	}

	@Override
	public void handle(String event) {
		eventHandlers.get(event).accept(null);
	}
}
