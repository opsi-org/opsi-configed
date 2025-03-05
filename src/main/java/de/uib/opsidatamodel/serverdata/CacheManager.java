/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides methods for storing and retrieving cache data.
 * <p>
 * Intended usage of this class is to store and retrieve persistent data (i.e.
 * data that lives for a long time; usually for as long as the process does).
 * <p>
 * {@link CacheManager} is based on Singleton design pattern, meaning there can
 * only be one instance throughout the lifetime of the application. The
 * implementation of Singleton design pattern, that {@link CacheManager} uses is
 * thread-safe. You can learn about it more here:
 * https://refactoring.guru/design-patterns/singleton/java/example#example-2
 */
public final class CacheManager {
	// volatile keyword is required for double check lock to work. Although,
	// SonarLint rule makes a valid point.
	@SuppressWarnings({ "java:S3077" })
	private static volatile CacheManager instance;
	private Map<CacheIdentifier, Object> cache = new ConcurrentHashMap<>();

	// This is a list of caches that we want to keep in a reload since it contains data about the GUI
	// that should not change.
	private List<CacheIdentifier> cacheToKeep = Arrays.asList(CacheIdentifier.HOST_DISPLAY_FIELDS,
			CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_LOCALBOOT_PRODUCTS,
			CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_NETBOOT_PRODUCTS);

	private CacheManager() {
	}

	/**
	 * Retrieves the current instance of {@link CacheManager}.
	 * <p>
	 * {@link CacheManager} is based on a thread-safe Singleton design pattern:
	 * https://refactoring.guru/design-patterns/singleton/java/example#example-2
	 * 
	 * @return current instance of {@link CacheManager}
	 */
	public static CacheManager getInstance() {
		CacheManager result = instance;
		if (result != null) {
			return result;
		}
		synchronized (CacheManager.class) {
			if (instance == null) {
				instance = new CacheManager();
			}
			return instance;
		}
	}

	/**
	 * Internally cache data.
	 * 
	 * @param <T>        type of data to store internally in cache
	 * @param identifier for identifying stored cache data
	 * @param data       to store internally
	 */
	public <T> void setCachedData(CacheIdentifier identifier, T data) {
		if (data != null) {
			cache.put(identifier, data);
		}
	}

	/**
	 * Retrieve internally stored data.
	 * 
	 * @param <T>        type of data to retrieve from internal cache
	 * @param identifier for identifying stored cache data
	 * @param dataClass  class type of data, that is stored internally
	 * @return internally stored data
	 */
	public <T> T getCachedData(CacheIdentifier identifier, Class<T> dataClass) {
		return dataClass.cast(cache.get(identifier));
	}

	/**
	 * Clear some internally cached data based on {@link CacheIdentifier}.
	 * 
	 * @param identifier cache data to clear
	 */
	public void clearCachedData(CacheIdentifier identifier) {
		cache.remove(identifier);
	}

	/**
	 * Clears all internally cached data.
	 */
	public void clearAllCachedData() {
		cache.clear();
	}

	public void clearForReload() {
		cache.keySet().retainAll(cacheToKeep);
	}

	public boolean isDataCached(Collection<CacheIdentifier> identifiers) {
		for (CacheIdentifier identifier : identifiers) {
			if (cache.get(identifier) == null) {
				return false;
			}
		}

		return true;
	}

	public boolean isDataCached(CacheIdentifier identifier) {
		return cache.get(identifier) != null;
	}
}
