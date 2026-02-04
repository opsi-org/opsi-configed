/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.provider;

import java.util.Map;
import java.util.function.Supplier;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;

public interface MapRetriever {
	void reloadMap();

	Map<String, Map<String, Object>> retrieveMap();

	static MapRetriever createMapRetriever(Supplier<Map<String, ? extends Map<String, ? extends Object>>> mapSupplier) {
		return new MapRetriever() {
			@Override
			public void reloadMap() {
				// no reload
			}

			@Override
			public Map<String, Map<String, Object>> retrieveMap() {
				return POJOReMapper.remap(mapSupplier.get());
			}
		};
	}

	// The reloadEvent may be a ReloadEvent or a CacheIdentifier
	static MapRetriever createMapRetriever(Object reloadEvent,
			Supplier<Map<String, ? extends Map<String, ? extends Object>>> mapSupplier) {
		return new MapRetriever() {
			@Override
			public void reloadMap() {
				PersistenceControllerFactory.getPersistenceController().reloadData(reloadEvent.toString());
			}

			@Override
			public Map<String, Map<String, Object>> retrieveMap() {
				return POJOReMapper.remap(mapSupplier.get());
			}
		};
	}
}
